package com.intouch.IntouchApps.config;

import com.intouch.IntouchApps.constants.CustomHeaders;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component//introduced to being able to access custom headers in different layers of the app loosely for single request scope
public class RequestMetadataInterceptor implements HandlerInterceptor {

    private final RequestMetadataContext requestMetadataContext;

    public RequestMetadataInterceptor(RequestMetadataContext requestMetadataContext) {
        this.requestMetadataContext = requestMetadataContext;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        //update the state of the request scoped bean
        requestMetadataContext.setClientType(request.getHeader(CustomHeaders.CLIENT_TYPE));
//        requestMetadataContext.setAppVersion(request.getHeader(CustomHeaders.APP_VERSION));
//        requestMetadataContext.setDeviceId(request.getHeader(CustomHeaders.DEVICE_ID));
//        requestMetadataContext.setLocale(request.getHeader(CustomHeaders.LOCALE));
//        requestMetadataContext.setTenantId(request.getHeader(CustomHeaders.TENANT_ID));
        return true;
    }
}
