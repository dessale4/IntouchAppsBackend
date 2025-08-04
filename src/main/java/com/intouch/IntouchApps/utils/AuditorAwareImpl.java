package com.intouch.IntouchApps.utils;

import com.intouch.IntouchApps.user.User;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;
@Component
public class AuditorAwareImpl implements AuditorAware<String> {
//    @Override
//    public Optional<String> getCurrentAuditor() {
//        return Optional.of("appSupport");
//    }
@Override
public Optional<String> getCurrentAuditor() {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if(authentication ==null || !authentication.isAuthenticated())
        throw new RuntimeException("Not allowed operation");
    User principal = (User)authentication.getPrincipal();
    return Optional.of(principal.getPublicUserName());
}
}
