package com.intouch.IntouchApps.user;

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
@Table(name = "Token_TBL")
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String token;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime validatedAt;
    private String creationReason;
    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;
}
