package org.example.expert.domain.user.dto.response;

import lombok.Getter;

@Getter
public class UserProfileImgResponse {

    private final Long id;
    private final String imgUrl;

    public UserProfileImgResponse(Long id, String imgUrl) {
        this.id = id;
        this.imgUrl = imgUrl;
    }

}
