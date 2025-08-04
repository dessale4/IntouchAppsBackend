package com.intouch.IntouchApps.payments.paypal;

import com.intouch.IntouchApps.payments.AppPayment;
import com.paypal.base.rest.PayPalRESTException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("paypalPay")
public class PaypalController {
    private final PaypalPaymentService paypalService;
    @PostMapping(value = "/payment/create")
    public PaypalPaymentResponse createPayment(@RequestBody PaypalPaymentRequest paypalPaymentRequest, HttpServletRequest httpServletRequest) throws PayPalRESTException {
     return paypalService.createPayment(paypalPaymentRequest, httpServletRequest);
    }
    @PostMapping(value = "/payment/success")
    public PaypalPaymentResponse paymentSuccess(@RequestBody AppPayment appPayment) throws PayPalRESTException {
        return paypalService.executePayment(appPayment);
    }
    @GetMapping("/payment/cancel")
    public void paymentCancel(){
        log.info("payment cancelled===");
    }
    @GetMapping("/payment/error")
    public void paymentError(){
        log.info("payment Error===");
    }
}
