package com.intouch.IntouchApps.payments.stripe;

import com.intouch.IntouchApps.payments.AppPayment;
import com.intouch.IntouchApps.payments.paypal.PaypalPaymentResponse;
import com.paypal.base.rest.PayPalRESTException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("stripePay")
public class StripePaymentController {
    private final StripePaymentService stripePaymentService;
    @PostMapping(value = "/payment/create")
    public ResponseEntity<StripePaymentResponse> createStripePayment(@RequestBody StripePaymentRequest stripePaymentRequest, HttpServletRequest request){
        StripePaymentResponse stripePayment = stripePaymentService.createStripePayment(stripePaymentRequest, request);
       return ResponseEntity
               .status(HttpStatus.OK)
               .body(stripePayment);
    }
    @PostMapping(value = "/payment/success")
    public ResponseEntity<Void> paymentSuccess(@RequestBody AppPayment appPayment) throws PayPalRESTException {
        stripePaymentService.recordPayment(appPayment);
        return ResponseEntity
                .status(HttpStatus.CREATED).build();
    }
}
