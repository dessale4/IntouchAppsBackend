package com.intouch.IntouchApps.payments.paypal;

import com.intouch.IntouchApps.payments.AppPayment;
import com.intouch.IntouchApps.payments.AppPaymentRepository;
//import com.intouch.IntouchApps.security.StringEncryptConverter;
import com.intouch.IntouchApps.user.User;
import com.intouch.IntouchApps.user.UserRepository;
import com.intouch.IntouchApps.utils.AppDateUtil;
import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@RefreshScope
public class PaypalPaymentService {
    private final APIContext apiContext;
    private final UserRepository userRepository;
    private final AppPaymentRepository appPaymentRepository;
    private final StandardPBEStringEncryptor standardPBEStringEncryptor;
    @Value("${application.payment.paypal.cancelUrl}")
    private String cancelUrl;
    @Value("${application.payment.paypal.successUrl}")
    private  String  successUrl;
    public PaypalPaymentResponse createPayment(
            PaypalPaymentRequest paypalPaymentRequest,
            HttpServletRequest httpServletRequest){
        User payingUser = userRepository.findByEmail(httpServletRequest.getUserPrincipal().getName()).orElseThrow(()->new RuntimeException("Account not found with the provided information: " + standardPBEStringEncryptor.decrypt(httpServletRequest.getUserPrincipal().getName())));
        User payToUser = null;
        if(paypalPaymentRequest.isPayingToSomeone()){
            if(paypalPaymentRequest.getPayToEmail() != null && paypalPaymentRequest.getPayToEmail() != ""){
                payToUser = userRepository.findByEmail(standardPBEStringEncryptor.encrypt(paypalPaymentRequest.getPayToEmail().toLowerCase())).orElseThrow(()->new RuntimeException("Account not found with the provided information: " + paypalPaymentRequest.getPayToEmail().toLowerCase()));
            }else{
                payToUser = userRepository.findByPublicUserName(paypalPaymentRequest.getPayToUserName()).orElseThrow(()->new RuntimeException("Account not found with the provided information: " + paypalPaymentRequest.getPayToUserName()));
            }
        }
        PaypalPaymentResponse paymentResponse;
        try{
        Amount amount = new Amount();
        amount.setCurrency(paypalPaymentRequest.getCurrency());
        amount.setTotal(String.format(Locale.forLanguageTag(paypalPaymentRequest.getCurrency()),"%.2f", paypalPaymentRequest.getAmount()));
        Transaction transaction = new Transaction();
        transaction.setDescription(paypalPaymentRequest.getDescription());
        transaction.setAmount(amount);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod(paypalPaymentRequest.getPayMethod());

        Payment payment = new Payment();
        payment.setIntent(paypalPaymentRequest.getIntent());
        payment.setPayer(payer);
        payment.setTransactions(transactions);
            String requestBaseUrl = httpServletRequest.getRequestURL().substring(0, httpServletRequest.getRequestURL().indexOf(httpServletRequest.getServletPath()));
            RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(requestBaseUrl + cancelUrl);
        redirectUrls.setReturnUrl(requestBaseUrl + successUrl);
        payment.setRedirectUrls(redirectUrls);
        Payment createdPayment = payment.create(apiContext);

            for(Links link: createdPayment.getLinks()){
                if(link.getRel().equals("approval_url")){
                    log.info("payment made\n"+ link.getHref());
                    paymentResponse = new PaypalPaymentResponse();
                    paymentResponse.setMessage("Payment creation succeeded");
                    paymentResponse.setPaymentLink(link.getHref());
                    paymentResponse.setPaymentSuccessLink(successUrl);
                    paymentResponse.setPaymentCancelLink(cancelUrl);
                    paymentResponse.setPayByEmail(payingUser.getEmail());
                    paymentResponse.setPayByUserName(payingUser.getPublicUserName());
                    paymentResponse.setPayToEmail(payToUser ==null ? standardPBEStringEncryptor.decrypt(payingUser.getEmail()):standardPBEStringEncryptor.decrypt(payToUser.getEmail()));
                    paymentResponse.setPayToUserName(payToUser ==null ? payingUser.getPublicUserName() : payToUser.getPublicUserName());
                    paymentResponse.setPayingToSomeone(paypalPaymentRequest.isPayingToSomeone());
                    return paymentResponse;
                }
            }
            paymentResponse = new PaypalPaymentResponse();
            paymentResponse.setMessage("Payment creation failed");
            return paymentResponse;
        }catch (PayPalRESTException e){
            log.error("payment error occurred");
            paymentResponse = new PaypalPaymentResponse();
            paymentResponse.setMessage("Error in Payment creation \n" + e.getMessage());
        }
        return paymentResponse;
    }

    public PaypalPaymentResponse executePayment(
            AppPayment appPayment
        ) throws PayPalRESTException
    {
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
        Payment payment = new Payment();
        payment.setId(appPayment.getPaymentId());
        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(appPayment.getPayerId());
        Payment executedPayment = payment.execute(apiContext, paymentExecution);
        PaypalPaymentResponse paymentResponse = paymentResponse(executedPayment, appPayment.getSubscriptionAmount(), appPayment.getSubscriptionMonthCount(), storedUser);
        appPayment.setSubscriptionEmailAccount(standardPBEStringEncryptor.encrypt(appPayment.getSubscriptionEmailAccount().toLowerCase()));
        appPayment.setPayToEmail(storedUser.getEmail());
        appPayment.setPayToUserName(storedUser.getPublicUserName());
        appPaymentRepository.save(appPayment);
        return paymentResponse;
    }

    public PaypalPaymentResponse paymentResponse(Payment payment, BigDecimal subscriptionAmount, Long subscriptionMonthCount, User storedUser){
        PaypalPaymentResponse paymentResponse = new PaypalPaymentResponse();
            if(payment.getState().equals("approved")){
                log.info("payment succeeded===");
                paymentResponse.setMessage("payment execution succeeded");
                updateUserSubscriptionStatus(subscriptionAmount, subscriptionMonthCount, storedUser);
            }else {
                log.error("Error occurred: ");
                paymentResponse.setMessage("payment execution failed");
            }
        return paymentResponse;
    }
    private void updateUserSubscriptionStatus(BigDecimal subscriptionAmount,
                                             Long subscriptionMonthCount,
                                             User storedUser
    ){
        try {
//            LocalDateTime subscriptionEndDate = storedUser.getSubscriptionEndDate();
//            LocalDateTime localDateTimeToSave = (subscriptionEndDate != null && AppDateUtil.getCurrentUTCLocalDateTime().isBefore(subscriptionEndDate)) ? subscriptionEndDate.plusMonths(subscriptionMonthCount) : AppDateUtil.getCurrentUTCLocalDateTime().plusMonths(subscriptionMonthCount);
//            storedUser.setSubscribed(true);
//            storedUser.setSubscriptionEndDate(localDateTimeToSave);
            storedUser.setLastModifiedDate(AppDateUtil.getCurrentUTCLocalDateTime());
            User updateUser = userRepository.save(storedUser);
        }catch (RuntimeException exception){
            throw  new RuntimeException("Subscription updation failed due to " + exception.getMessage());
        }
    }
}
