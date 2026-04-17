package com.intouch.IntouchApps.user;

import com.intouch.IntouchApps.common.BaseEntity;
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
@Table(name = "Subscription_TBL")
public class Subscription extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private String ownerPublicUserName;
    @Column(nullable = false)
    private String subscriptionProductName;
    @Column(nullable = false)
    private Instant expirationDate;
    private String noteOnUpdate;
    private Integer productPurchaseCount;

}
