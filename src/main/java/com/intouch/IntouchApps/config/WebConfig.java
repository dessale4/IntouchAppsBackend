package com.intouch.IntouchApps.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration//Registering web level custom beans like the request scoped custom header metadatas
public class WebConfig implements WebMvcConfigurer {

    private final RequestMetadataInterceptor requestMetadataInterceptor;

    public WebConfig(RequestMetadataInterceptor requestMetadataInterceptor) {
        this.requestMetadataInterceptor = requestMetadataInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestMetadataInterceptor);
    }
}
