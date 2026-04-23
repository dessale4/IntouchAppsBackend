package com.intouch.IntouchApps.user;

import com.intouch.IntouchApps.adminAccess.RuntimeConfigService;
import com.intouch.IntouchApps.security.JwtService;
import com.intouch.IntouchApps.security.SecurityUtils;
import com.intouch.IntouchApps.utils.AppDateUtil;
import com.intouch.IntouchApps.utils.AppPhoneUtil;
import com.intouch.IntouchApps.utils.ConstantsUtil;
import com.intouch.IntouchApps.utils.UserAndRolesUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@RefreshScope
@Transactional
public class AppUsersService {
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final StandardPBEStringEncryptor standardPBEStringEncryptor;
    private final JwtService jwtService;
    private final SubscriptionService subscriptionService;
    private final SecurityUtils securityUtils;
    @Value("${application.payment.commonSubscriptionKey}")
    private String commonSubKey;
    private final RuntimeConfigService runtimeConfigService;
    private final UserRoleRepository userRoleRepository;
    private final AgePolicyService agePolicyService;
    @Value("${application.payment.enabled}")
    private Boolean isAppPaymentEnabled;
    public List<AccountDTO> getAppUsernames() {
        return userRepository.findAll().stream()
                .map((u -> mapUserToAccountDTO(u)))
                .collect(Collectors.toList());
    }
    private AccountDTO mapUserToAccountDTO(User user) {
        return AccountDTO.builder()
                .accountEmail(standardPBEStringEncryptor.decrypt(user.getEmail()))
                .accountUsername(user.getUserName())
                .build();
    }

    public UserDTO loadLoggedUserByUserEmail(HttpServletRequest request) {
//        String jwtToken = null;
//        if (request.getCookies() != null) {
//            jwtToken = Arrays.stream(request.getCookies())
//                    .filter(c -> c.getName().equals("jwt"))
//                    .findFirst()
//                    .map(Cookie::getValue)
//                    .orElse(null);
//        }
//        if(jwtToken == null){
//            String authHeader = request.getHeader(AUTHORIZATION);
//            if (authHeader != null && authHeader.startsWith("Bearer ")) {
//                jwtToken = authHeader.substring(7);
//            }
//        }
//        String userEmail = jwtService.extractUsername(jwtToken, false);
        String userEmail = securityUtils.getCurrentUserEmail();
        String decryptedEmail = standardPBEStringEncryptor.decrypt(userEmail);
        User user = userRepository.findByEmailWithActiveRoles(userEmail).orElseThrow(() -> new UsernameNotFoundException("No user was found with email: " + decryptedEmail));
//        boolean subscriptionIsExpired = user.getSubscriptionEndDate() !=null && AppDateUtil.getCurrentUTCLocalDateTime().isAfter(user.getSubscriptionEndDate());
        Set<String> commonAppAccess = Set.of(UserAndRolesUtil.subscriptionMap().get(commonSubKey));
        AgePolicyResponse agePolicy = user.getDateOfBirth() !=null ?  agePolicyService.evaluate(user.getDateOfBirth()) : null;
        return UserDTO.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(decryptedEmail)
                .username(user.getUserName())
                .phoneNumber(user.getPhoneNumber())
                .enabled(user.isEnabled())
                .screensLockPin(user.getScreensLockPin())
//                .isSubscribed(subscriptionIsExpired ? false: user.isSubscribed())
                .accountLocked(user.isAccountLocked())
                .roles(user.getUserRoles().stream().map(r -> r.getRole().getName()).collect(Collectors.toSet()))
                .appAccesses(UserAndRolesUtil.adminSetEmails.contains(standardPBEStringEncryptor.decrypt(user.getEmail())) ? commonAppAccess : subscriptionService.getUserActiveSubscriptions(user.getUserName()))
//                .subscriptionEndDate(user.getSubscriptionEndDate())
//                .paymentEnabled(isAppPaymentEnabled)//can be used if Config Server is enabled
                .paymentEnabled(Boolean.parseBoolean((String) runtimeConfigService.getProperty(ConstantsUtil.APPLICATION_PAYMENT_ENABLED_PROP)))
                .agePolicy(agePolicy)
                .build();
    }

    public Integer updateUserScreenLockPin(Integer screenLockPin) {
        String userEmail = securityUtils.getCurrentUserEmail();
        String decryptedEmail = standardPBEStringEncryptor.decrypt(userEmail);
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new UsernameNotFoundException("No user was found with email: " + decryptedEmail));
        user.setScreensLockPin(screenLockPin);
        userRepository.save(user);
        return screenLockPin;
    }

    public void validateUserEmailedToken(String token, String emailReason) {
        String authEmail = securityUtils.getCurrentUserEmail();
        VerificationToken savedVerificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        User user = savedVerificationToken.getUser();
        if (!user.getEmail().equals(authEmail)) {
            throw new RuntimeException("Operation denied");
        }
        if (!savedVerificationToken.getCreationReason().equals(emailReason)) {
            throw new RuntimeException(savedVerificationToken.getToken() + " is not " + emailReason + " token");
        }
        if (AppDateUtil.getCurrentUtcInstant().isAfter(savedVerificationToken.getExpiresAt())) {
            throw new RuntimeException("Token has been already expired.");
        }
        savedVerificationToken.setValidatedAt(AppDateUtil.getCurrentUtcInstant());
        tokenRepository.save(savedVerificationToken);
    }

    public AgePolicyResponse updateUserBirthDay(LocalDate dateOfBirth, String phoneNumber) {
        AgePolicyResponse agePolicy = agePolicyService.evaluate(dateOfBirth);

        if (!agePolicy.canRegister()) {
            throw new IllegalArgumentException("You must be at least 13 years old to create an account");
        }
        String currentUserEmail = securityUtils.getCurrentUserEmail();
        User user = userRepository.findByEmailWithActiveRoles(currentUserEmail).orElseThrow(() -> new RuntimeException("Account not found"));
        user.setDateOfBirth(dateOfBirth);
        user.setDobVerified(true);
        if(phoneNumber != null){
            String normalizedPhone = null;
            try {
                normalizedPhone = AppPhoneUtil.normalizeToE164OrNull(phoneNumber);
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException("Please enter a valid phone number including country code.");
            }
            user.setPhoneNumber(normalizedPhone);
            user.setPhoneVerified(false);
        }
        userRepository.save(user);
        return agePolicy;
    }
}

