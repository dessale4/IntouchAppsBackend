package com.intouch.IntouchApps.payments.stripe;

import com.intouch.IntouchApps.payments.AppPayment;
import com.intouch.IntouchApps.payments.AppPaymentRepository;
import com.intouch.IntouchApps.user.User;
import com.intouch.IntouchApps.user.UserRepository;
import com.intouch.IntouchApps.utils.AppDateUtil;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StripePaymentService {
    private final UserRepository userRepository;
    private final AppPaymentRepository appPaymentRepository;
    private final StandardPBEStringEncryptor standardPBEStringEncryptor;
    @Value("${application.payment.stripe.secret-key}")
    private String secretKey;
    @Value("${application.payment.stripe.cancelUrl}")
    private String cancelUrl;
    @Value("${application.payment.stripe.successUrl}")
    private  String  successUrl;
    public StripePaymentResponse createStripePayment(StripePaymentRequest stripePaymentRequest, HttpServletRequest httpServletRequest){
        User payingUser = userRepository.findByEmail(httpServletRequest.getUserPrincipal().getName()).orElseThrow(()->new RuntimeException("Account not found with the provided information: " + standardPBEStringEncryptor.decrypt(httpServletRequest.getUserPrincipal().getName())));
        User payToUser = null;
        if(stripePaymentRequest.isPayingToSomeone()){
            if(stripePaymentRequest.getPayToEmail() != null && stripePaymentRequest.getPayToEmail() != ""){
                payToUser = userRepository.findByEmail(standardPBEStringEncryptor.encrypt(stripePaymentRequest.getPayToEmail().toLowerCase())).orElseThrow(()->new RuntimeException("Account not found with the provided information: " + stripePaymentRequest.getPayToEmail().toLowerCase()));
            }else{
                payToUser = userRepository.findByPublicUserName(stripePaymentRequest.getPayToUserName()).orElseThrow(()->new RuntimeException("Account not found with the provided information: " + stripePaymentRequest.getPayToUserName()));
            }
        }
        String requestBaseUrl = httpServletRequest.getRequestURL().substring(0, httpServletRequest.getRequestURL().indexOf(httpServletRequest.getServletPath()));
        Stripe.apiKey=secretKey;
        SessionCreateParams.LineItem.PriceData.ProductData productData = SessionCreateParams.LineItem.PriceData.ProductData.builder()
                .setName(stripePaymentRequest.getName()).build();
        SessionCreateParams.LineItem.PriceData priceData = SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency(stripePaymentRequest.getCurrency() == null ? "USD" : stripePaymentRequest.getCurrency())
                .setUnitAmount(stripePaymentRequest.getAmount())
                .setProductData(productData)
                .build();
        SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                .setQuantity(stripePaymentRequest.getQuantity())
                .setPriceData(priceData)
                .build();
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(requestBaseUrl + successUrl)
                .setCancelUrl(requestBaseUrl + cancelUrl)
                .addLineItem(lineItem)
                .build();
        Session session = null;
        try{
            session = Session.create(params);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

        return StripePaymentResponse.builder()
                .status("SUCCESS")
                .message("Payment session created")
                .sessionId(session.getId())
                .sessionUrl(session.getUrl())
                .paymentSuccessLink(successUrl)
                .paymentCancelLink(cancelUrl)
                .payByEmail(standardPBEStringEncryptor.decrypt(payingUser.getEmail()))
                .payToEmail(payToUser != null ? standardPBEStringEncryptor.decrypt(payToUser.getEmail()) : standardPBEStringEncryptor.decrypt(payingUser.getEmail()))
                .payByUserName(payingUser.getPublicUserName())
                .payToUserName(payToUser != null ? payToUser.getPublicUserName() : payingUser.getPublicUserName())
                .payingToSomeone(stripePaymentRequest.isPayingToSomeone())
                .build();
    }

    public void recordPayment(AppPayment appPayment) {
        User storedUser  = null;
        if(appPayment.isPayingToSomeone()){
            if(appPayment.getPayToEmail() != null && appPayment.getPayToEmail() != ""){
                storedUser = userRepository.findByEmail(standardPBEStringEncryptor.encrypt(appPayment.getPayToEmail().toLowerCase())).orElseThrow(()->new RuntimeException("Account not found with the provided information \n" + appPayment.getPayToEmail().toLowerCase()));
            }else{
                storedUser = userRepository.findByPublicUserName(appPayment.getPayToUserName()).orElseThrow(()->new RuntimeException("Account not found with the provided information \n" + appPayment.getPayToUserName()));
            }
        }else{
            storedUser  = userRepository.findByEmail(standardPBEStringEncryptor.encrypt(appPayment.getSubscriptionEmailAccount().toLowerCase())).orElseThrow(()->new UsernameNotFoundException("No account with email " + appPayment.getSubscriptionEmailAccount().toLowerCase()));
        }
        try{
            appPayment.setSubscriptionEmailAccount(standardPBEStringEncryptor.encrypt(appPayment.getSubscriptionEmailAccount().toLowerCase()));
            appPayment.setPayToEmail(storedUser.getEmail());
            appPayment.setPayToUserName(storedUser.getPublicUserName());
            appPaymentRepository.save(appPayment);
            updateUserSubscriptionStatus(appPayment.getSubscriptionAmount(),appPayment.getSubscriptionMonthCount(), storedUser);
        }catch(Exception exception){
            throw  new RuntimeException("Subscription updation failed due to " + exception.getMessage());
        }
    }
    private void updateUserSubscriptionStatus(BigDecimal subscriptionAmount,
                                              Long subscriptionMonthCount,
                                              User storedUser
    ){
        try {
            LocalDateTime subscriptionEndDate = storedUser.getSubscriptionEndDate();
            LocalDateTime localDateTimeToSave = (subscriptionEndDate != null && AppDateUtil.getCurrentUTCLocalDateTime().isBefore(subscriptionEndDate)) ? subscriptionEndDate.plusMonths(subscriptionMonthCount) : AppDateUtil.getCurrentUTCLocalDateTime().plusMonths(subscriptionMonthCount);
            storedUser.setSubscribed(true);
            storedUser.setSubscriptionEndDate(localDateTimeToSave);
            storedUser.setLastModifiedDate(AppDateUtil.getCurrentUTCLocalDateTime());
            User updateUser = userRepository.save(storedUser);
        }catch (RuntimeException exception){
            throw  new RuntimeException("Subscription updation failed due to " + exception.getMessage());
        }
    }
}
