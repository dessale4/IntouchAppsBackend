package com.intouch.IntouchApps.config;

import com.paypal.base.rest.APIContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaypalConfig {
    @Value("${application.payment.paypal.client_id}")
    private String clientId;
    @Value("${application.payment.paypal.client_secret}")
    private String clientSecret;
    @Value("${application.payment.paypal.mode}")
    private String mode;
    @Bean
    public APIContext apiContext(){
        return new APIContext(clientId, clientSecret, mode);
    }
}
