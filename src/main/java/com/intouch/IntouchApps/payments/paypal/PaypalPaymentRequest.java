package com.intouch.IntouchApps.payments.paypal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaypalPaymentRequest {
    private BigDecimal amount;
    private String currency;
    private String payMethod;
    private String intent;
    private String description;
    private String payByEmail;
    private String payToUserName;
    private String payToEmail;
    private boolean payingToSomeone;
}
