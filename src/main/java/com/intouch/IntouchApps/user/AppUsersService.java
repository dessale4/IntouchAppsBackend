package com.intouch.IntouchApps.user;

import com.intouch.IntouchApps.adminAccess.RuntimeConfigService;
import com.intouch.IntouchApps.security.JwtService;
import com.intouch.IntouchApps.utils.AppDateUtil;
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

import java.security.Principal;
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
    @Value("${application.payment.commonSubscriptionKey}")
    private String commonSubKey;
    private final RuntimeConfigService runtimeConfigService;
    @Value("${application.payment.enabled}")
    private Boolean isAppPaymentEnabled;
    public List<AccountDTO> getAppUsernames(){
        return userRepository.findAll().stream()
                .map((u->mapUserToAccountDTO(u)))
                .collect(Collectors.toList());
    }
    private AccountDTO mapUserToAccountDTO(User user){
    return AccountDTO.builder()
            .accountEmail(standardPBEStringEncryptor.decrypt(user.getEmail()))
            .accountUsername(user.getPublicUserName())
            .build();
    }

    public UserDTO loadUserByUsername(HttpServletRequest request, Principal principal) {
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
        String userEmail = principal.getName();
        String decryptedEmail = standardPBEStringEncryptor.decrypt(userEmail);
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new UsernameNotFoundException("No user was found with email: " + decryptedEmail));
//        ====== consider returning UserDTO instead of UserDetails======
//        boolean subscriptionIsExpired = user.getSubscriptionEndDate() !=null && AppDateUtil.getCurrentUTCLocalDateTime().isAfter(user.getSubscriptionEndDate());
        Set<String> commonAppAccess = Set.of(UserAndRolesUtil.subscriptionMap().get(commonSubKey));
        System.out.println("isAppPaymentEnabled => " + isAppPaymentEnabled);
        UserDTO userDTO = UserDTO.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
//                .email(standardPBEStringEncryptor.decrypt(userEmail))
                .email(decryptedEmail)
                .username(user.getPublicUserName())
                .enabled(user.isEnabled())
                .screensLockPin(user.getScreensLockPin())
//                .isSubscribed(subscriptionIsExpired ? false: user.isSubscribed())
                .accountLocked(user.isAccountLocked())
                .roles(user.getRoles().stream().map(r->r.getName()).collect(Collectors.toSet()))
                .appAccesses(UserAndRolesUtil.adminSetEmails.contains(standardPBEStringEncryptor.decrypt(user.getEmail())) ? commonAppAccess : subscriptionService.getUserActiveSubscriptions(user.getPublicUserName()))
//                .subscriptionEndDate(user.getSubscriptionEndDate())
//                .paymentEnabled(isAppPaymentEnabled)//can be used if Config Server is enabled
                .paymentEnabled(Boolean.parseBoolean((String) runtimeConfigService.getProperty(ConstantsUtil.APPLICATION_PAYMENT_ENABLED_PROP)))
                .build();
        return userDTO;
    }

    public Integer updateUserScreenLockPin(Principal principal, Integer screenLockPin) {
        String userEmail = principal.getName();
        String decryptedEmail = standardPBEStringEncryptor.decrypt(userEmail);
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new UsernameNotFoundException("No user was found with email: " + decryptedEmail));
        user.setScreensLockPin(screenLockPin);
        userRepository.save(user);
        return screenLockPin;
    }
    public void validateUserEmailedToken(String token, Principal principal, String emailReason) {
        String authName = principal.getName();
        Token savedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        User user = savedToken.getUser();
        if (!user.getEmail().equals(authName)) {
            throw new RuntimeException("Operation denied");
        }
        if (!savedToken.getCreationReason().equals(emailReason)) {
            throw new RuntimeException(savedToken.getToken() + " is not " + emailReason+ " token");
        }
        if (AppDateUtil.getCurrentUTCLocalDateTime().isAfter(savedToken.getExpiresAt())) {
            throw new RuntimeException("Token has been already expired.");
        }
        savedToken.setValidatedAt(AppDateUtil.getCurrentUTCLocalDateTime());
        tokenRepository.save(savedToken);
    }

}

