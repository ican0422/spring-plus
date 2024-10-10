package org.example.expert.domain.user.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserProfileImgResponse;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    // S3
    private final AmazonS3Client s3Client;

    // S3 버킷
    @Value("${s3.bucket}")
    private String bucket;

    @Transactional
    public UserProfileImgResponse saveProfileImages(Long userId, MultipartFile image) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        // 이미 이미지가 등록되어 있다면 등록된 이미지 삭제
        if (user.getImageUrl() != null) {
            // 기존 등록된 URL 가지고 이미지 원본 이름 가져오기
            String imageName = extractFileNameFromUrl(user.getImageUrl());

            // 가져온 이미지 원본 이름으로 S3 이미지 삭제
            s3Client.deleteObject(bucket, imageName);

            user.imagePublication(null);
            userRepository.save(user);
        }

        // 업로드한 파일의 S3 URL 주소
        String imageUrl = uploadImageToS3(image, bucket);

        user.imagePublication(imageUrl);
        User profileUser = userRepository.save(user);

        return new UserProfileImgResponse(profileUser.getId(), profileUser.getImageUrl());
    }

    public UserResponse getUser(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new InvalidRequestException("User not found"));
        return new UserResponse(user.getId(), user.getEmail());
    }

    @Transactional
    public void changePassword(long userId, UserChangePasswordRequest userChangePasswordRequest) {
        validateNewPassword(userChangePasswordRequest);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("User not found"));

        if (passwordEncoder.matches(userChangePasswordRequest.getNewPassword(), user.getPassword())) {
            throw new InvalidRequestException("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");
        }

        if (!passwordEncoder.matches(userChangePasswordRequest.getOldPassword(), user.getPassword())) {
            throw new InvalidRequestException("잘못된 비밀번호입니다.");
        }

        user.changePassword(passwordEncoder.encode(userChangePasswordRequest.getNewPassword()));
    }

    private static void validateNewPassword(UserChangePasswordRequest userChangePasswordRequest) {
        if (userChangePasswordRequest.getNewPassword().length() < 8 ||
                !userChangePasswordRequest.getNewPassword().matches(".*\\d.*") ||
                !userChangePasswordRequest.getNewPassword().matches(".*[A-Z].*")) {
            throw new InvalidRequestException("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.");
        }
    }

    /* 등록된 메뉴 기존 URL 원본 파일이름으로 디코딩 */
    private String extractFileNameFromUrl(String url) {
        try {
            // URL 마지막 슬래시의 위치를 찾아서 인코딩된 파일 이름 가져오기
            String encodedFileName = url.substring(url.lastIndexOf("/") + 1);

            // 인코딩된 파일 이름을 디코딩 해서 진짜 원본 파일 이름 가져오기
            return URLDecoder.decode(encodedFileName, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            // This shouldn't happen with UTF-8, but we need to handle the exception
            throw new RuntimeException("원본 파일 이름 변경 에러", e);
        }
    }

    /* 이미지 파일 이름 변경 */
    private String changeFileName(String originalFileName) {
        // 이미지 등록 날짜를 붙여서 리턴
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return LocalDateTime.now().format(formatter) + "_" + originalFileName;
    }

    /* 이미지를 등록하고 URL 추출 */
    private String uploadImageToS3(MultipartFile image, String bucket) throws IOException {
        // 이미지 이름 변경
        String originalFileName = image.getOriginalFilename();
        String fileName = changeFileName(originalFileName);

        // S3에 파일을 보낼 때 파일의 종류와 크기를 알려주기
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(image.getContentType());
        metadata.setContentLength(image.getSize());
        metadata.setContentDisposition("inline");

        // S3에 파일 업로드
        s3Client.putObject(bucket, fileName, image.getInputStream(), metadata);

        return s3Client.getUrl(bucket, fileName).toString();
    }
}
