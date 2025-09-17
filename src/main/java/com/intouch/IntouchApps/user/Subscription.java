package com.intouch.IntouchApps.user;

import com.intouch.IntouchApps.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "Subscription_TBL")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Subscription extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private String ownerPublicUserName;
    @Column(nullable = false)
    private String subscriptionProductName;
    @Column(nullable = false)
    private LocalDateTime expirationDate;
    private String noteOnUpdate;
    private Integer productPurchaseCount;

}
