package com.intouch.IntouchApps.payments.paypal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaypalPaymentResponse {
    private String message;
    private String paymentLink;
    private String paymentSuccessLink;
    private String paymentCancelLink;
    private String payToUserName;
    private String payToEmail;
    private String payByUserName;
    private String payByEmail;
    private boolean payingToSomeone;
}
