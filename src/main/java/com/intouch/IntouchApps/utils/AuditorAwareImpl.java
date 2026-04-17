package com.intouch.IntouchApps.utils;

import com.intouch.IntouchApps.security.CustomUserDetails;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated())
            throw new RuntimeException("Not allowed operation");
        // principal may already be a String
        Object principal = authentication.getPrincipal();
        String username;

        if (principal instanceof CustomUserDetails) {
            username = ((CustomUserDetails) principal).getDisplayUsername().toUpperCase();
        } else if (principal instanceof org.springframework.security.core.userdetails.User) {
            username = ((org.springframework.security.core.userdetails.User) principal).getUsername();
        } else if (principal instanceof String) {
            username = (String) principal;
        } else {
            throw new RuntimeException("Unknown principal type: " + principal.getClass());
        }

        return Optional.of(username);
    }
}
