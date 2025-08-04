package com.intouch.IntouchApps.payments;

import com.intouch.IntouchApps.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "AppPayment_TBL")
public class AppPayment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private String paymentMethod;
    @Column(nullable = false)
    private String paymentId;
    @Column(nullable = false)
    private String payerId;
    @Column(nullable = false)
    private BigDecimal subscriptionAmount;
    @Column(nullable = false)
    private Long subscriptionMonthCount;
    @Column(nullable = false)
    private String subscriptionEmailAccount;
    @Column(nullable = false)
    private String payToUserName;
    @Column(nullable = false)
    private String payToEmail;
    private boolean payingToSomeone;
}
