package com.intouch.IntouchApps.payments.stripe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StripePaymentResponse {
    private String status;
    private String message;
    private String sessionId;
    private String sessionUrl;
    private String paymentSuccessLink;
    private String paymentCancelLink;
    private String payToUserName;
    private String payToEmail;
    private String payByUserName;
    private String payByEmail;
    private boolean payingToSomeone;
}
