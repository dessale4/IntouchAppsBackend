package com.intouch.IntouchApps.user;

import com.intouch.IntouchApps.common.BaseEntity;
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
@Table(name = "RefreshToken_TBL")
public class RefreshToken extends BaseEntity {
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
