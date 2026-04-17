package com.intouch.IntouchApps.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {
    public CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new RuntimeException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails;
        }

        throw new RuntimeException("Unknown principal type: " + principal.getClass());
    }

    public String getCurrentUserEmail() {
        return getCurrentUserDetails().getEmail();
    }

    public String getCurrentUsername() {
        return getCurrentUserDetails().getDisplayUsername();
    }

    public Integer getCurrentUserId() {
        return getCurrentUserDetails().getUserId();
    }

    public String getCurrentFullName() {
        return getCurrentUserDetails().getFullName();
    }
}
