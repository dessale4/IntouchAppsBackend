package com.intouch.IntouchApps.user;

import com.intouch.IntouchApps.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
//@ToString(exclude = "userRoles")
//@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
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
