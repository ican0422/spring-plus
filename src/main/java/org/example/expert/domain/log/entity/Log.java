package org.example.expert.domain.log.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.expert.domain.log.eunm.LogSuccessfulStatus;
import org.example.expert.domain.user.entity.User;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "log")
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @Enumerated(EnumType.STRING)
    private LogSuccessfulStatus apiStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private final LocalDateTime createdAt = LocalDateTime.now();

    public Log(LogSuccessfulStatus apiStatus, User user) {
        this.apiStatus = apiStatus;
        this.user = user;
    }
}
