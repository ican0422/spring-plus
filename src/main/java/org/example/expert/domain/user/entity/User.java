package org.example.expert.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.entity.Timestamped;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "users")
public class User extends Timestamped {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nickname;
    @Column(unique = true)
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    public User(String nickname, String email, String password, UserRole userRole) {
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.userRole = userRole;
    }

    private User(Long id, String nickname, String email, UserRole userRole) {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
        this.userRole = userRole;
    }

    public static User fromAuthUser(AuthUser authUser) {
        Collection<? extends GrantedAuthority> authorities = authUser.getAuthorities();
        GrantedAuthority authority = authorities.stream().findFirst().orElseThrow(() -> new RuntimeException("권한 없음"));
        String userRole = authority.getAuthority();
        UserRole authRole;
        if (userRole.equals("ROLE_USER")) {
            authRole = UserRole.ROLE_USER;
        } else if (userRole.equals("ROLE_ADMIN")) {
            authRole = UserRole.ROLE_ADMIN;
        } else {
            throw new RuntimeException("잘못된 권한");
        }
        return new User(authUser.getId(), authUser.getNickname(), authUser.getEmail(), authRole);
    }

    public void changePassword(String password) {
        this.password = password;
    }

    public void updateRole(UserRole userRole) {
        this.userRole = userRole;
    }
}
