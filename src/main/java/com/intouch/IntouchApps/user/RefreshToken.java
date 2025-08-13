package com.intouch.IntouchApps.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, unique = true, length = 1024)
    private String jwtRefreshToken;
    private LocalDateTime expiresAt;
    @OneToOne
    private User user;
    @Column(nullable = false)
    private boolean revoked;
}
