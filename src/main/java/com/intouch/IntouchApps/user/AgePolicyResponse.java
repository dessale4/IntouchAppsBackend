package com.intouch.IntouchApps.user;
public record AgePolicyResponse(
        int age,
        boolean canRegister,
        boolean canUsePayments,
        boolean canAccessMatureContent,
        boolean canUsePersonalizedAds
) {}
