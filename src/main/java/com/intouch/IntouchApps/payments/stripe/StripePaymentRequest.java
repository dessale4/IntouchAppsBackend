package com.intouch.IntouchApps.payments.stripe;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StripePaymentRequest {
    private Long amount;
    private Long quantity;
    private String name;
    private String currency;
    private String payMethod;
    private String payByEmail;
    private String payToUserName;
    private String payToEmail;
    private boolean payingToSomeone;
}
