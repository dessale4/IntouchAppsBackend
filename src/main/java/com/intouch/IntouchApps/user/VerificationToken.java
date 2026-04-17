package com.intouch.IntouchApps.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
//@ToString(exclude = "userRoles")
//@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "Token_TBL")
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String token;
    private Instant createdAt;
    private Instant expiresAt;
    private Instant validatedAt;
    private String creationReason;
    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;
}
