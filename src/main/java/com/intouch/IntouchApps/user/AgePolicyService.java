package com.intouch.IntouchApps.user;
import com.intouch.IntouchApps.utils.AgeUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class AgePolicyService {

    public AgePolicyResponse evaluate(LocalDate dateOfBirth) {
        int age = AgeUtil.calculateAge(dateOfBirth);

        boolean canRegister = age >= 13;
        boolean canUsePayments = age >= 18;
        boolean canAccessMatureContent = age >= 18;
        boolean canUsePersonalizedAds = age >= 13;

        return new AgePolicyResponse(
                age,
                canRegister,
                canUsePayments,
                canAccessMatureContent,
                canUsePersonalizedAds
        );
    }
}
