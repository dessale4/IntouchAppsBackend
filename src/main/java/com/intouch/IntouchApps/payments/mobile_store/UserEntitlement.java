package com.intouch.IntouchApps.payments.mobile_store;

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
@Table(name="UserEntitlement_TBL")
public class UserEntitlement extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String rcAppUserId;
    @Column(unique = true, nullable = false)
    private String rcTransactionId;
    private String productId;
    private String platform;
    private String purchaserPublicUserName;
    private Boolean isAGift;
    private String beneficiary;
    private Boolean verifiedWithRc;
    private Boolean active;
//    private Instant purchaseDate;
    private Instant expirationDate; // optional
    private String statusInRc;
}
