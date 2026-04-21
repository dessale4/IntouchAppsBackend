package com.intouch.IntouchApps.auth;

import com.intouch.IntouchApps.config.RequestMetadataContext;
import com.intouch.IntouchApps.constants.RoleConstants;
import com.intouch.IntouchApps.email.AppEmail;
import com.intouch.IntouchApps.email.EmailService;
import com.intouch.IntouchApps.enums.JwtTokenType;
import com.intouch.IntouchApps.handler.AccountNotActivatedException;
import com.intouch.IntouchApps.role.Role;
import com.intouch.IntouchApps.role.RoleRepository;
import com.intouch.IntouchApps.security.CustomUserDetails;
import com.intouch.IntouchApps.security.JwtService;
import com.intouch.IntouchApps.user.*;
import com.intouch.IntouchApps.utils.AppDateUtil;
import com.intouch.IntouchApps.utils.AppPhoneUtil;
import com.intouch.IntouchApps.utils.PatternUtil;
import com.intouch.IntouchApps.utils.UserAndRolesUtil;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.security.SecureRandom;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.intouch.IntouchApps.constants.ClientType.MOBILE_CLIENT;
import static com.intouch.IntouchApps.constants.ClientType.WEB_CLIENT;
import static com.intouch.IntouchApps.email.EmailTemplateName.GENERIC_EMAIL_TEMPLATE;
import static com.intouch.IntouchApps.enums.JwtTokenType.ACCESS_TOKEN;
import static com.intouch.IntouchApps.enums.JwtTokenType.REFRESH_TOKEN;
import static org.springframework.http.HttpHeaders.SET_COOKIE;

@Service
@RequiredArgsConstructor
@RefreshScope
@Transactional
public class AuthenticationService {
    private final StandardPBEStringEncryptor standardPBEStringEncryptor;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRoleRepository userRoleRepository;
    private final AgePolicyService agePolicyService;
    private final RequestMetadataContext requestMetadataContext;//request scoped bean
    @Value("${application.mailing.backend.email_validation_key}")
    private String validateEmail;
    @Value("${server.ssl.enabled:false}")
    private boolean isSSLEnabled;
    @Value("${application.mailing.backend.account_delete_key}")
    private String deleteAccountEmail;
    @Value("${application.mailing.backend.password_reset_key}")
    private String resetPassword;
    @Value("${application.mailing.frontend.activation_url}")
    private String activationUrl;
    @Value("${application.mailing.backend.app_config_key}")
    private String appConfigKey;
    @Value("${application.mailing.backend.app_config_email}")
    private String appConfigEmail;
    private final RefreshTokenService refreshTokenService;

    public void register(RegistrationRequest request) throws MessagingException {
        String normalizedPhone = null;
        try {
            normalizedPhone = AppPhoneUtil.normalizeToE164OrNull(request.getPhoneNumber());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Please enter a valid phone number including country code.");
        }
        if (request.getUserName().contains("@")) {
            throw new RuntimeException("It is not allowed to have @ symbol in public userName.");
        }
        String encryptedEmail = standardPBEStringEncryptor.encrypt(request.getEmail().toLowerCase());
        if (userRepository.existsByEmail(encryptedEmail)) {
            throw new RuntimeException(String.format("An account with email  %s already exists.", request.getEmail()));
        }
        if (userRepository.existsByUserName(request.getUserName().toLowerCase())) {
            throw new RuntimeException(String.format("An account with userName  %s already exists.", request.getUserName()));
        }
        AgePolicyResponse agePolicy = agePolicyService.evaluate(request.getDateOfBirth());

        if (!agePolicy.canRegister()) {
            throw new IllegalArgumentException("You must be at least 13 years old to create an account");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(normalizedPhone)
                .email(encryptedEmail)
                .userName(request.getUserName().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .dateOfBirth(request.getDateOfBirth())
                .accountLocked(false)
                .dobVerified(true)
                .enabled(false)
                .createdDate(AppDateUtil.getCurrentUTCLocalDateTime())
                .lastModifiedDate(AppDateUtil.getCurrentUTCLocalDateTime())
                .build();

        user = userRepository.save(user);
        assignDefaultUserRole(user);
        sendEmail(user, validateEmail);
    }

    private void assignDefaultUserRole(User user) {
        Role userRole = roleRepository.findByName(RoleConstants.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Something went wrong"));
        UserRole assignment = UserRole.builder()
                .user(user)
                .role(userRole)
                .assignedBy("SYSTEM")
                .assignedAt(Instant.now())
                .active(true)
                .build();
        userRoleRepository.save(assignment);
    }

    //user is as is fetched from DB
    private void sendEmail(User user, String emailReason) throws MessagingException {
        var newToken = generateAndSaveActivationToken(user, emailReason);
        String decryptedEmail = standardPBEStringEncryptor.decrypt(user.getEmail());
        boolean isEmailAdminSetEmail = UserAndRolesUtil.adminSetEmails.contains(decryptedEmail);
        String verifyingEmail = emailReason.equals(appConfigKey) ? appConfigEmail : isEmailAdminSetEmail ? UserAndRolesUtil.adminSetVerifyingEmail.toLowerCase() : decryptedEmail;
        // send email
        AppEmail appEmail = AppEmail.builder()
                .to(verifyingEmail)
                .username(user.fullName())
                .emailTemplate(GENERIC_EMAIL_TEMPLATE)
                .confirmationUrl(activationUrl)
                .activationCode(newToken)
                .subject(emailReason.toUpperCase())
                .messageTitle(emailReason.toUpperCase())
                .confirmationText(emailReason.toUpperCase())
                .message(emailReason.toUpperCase())
                .build();
        emailService.sendEmail(appEmail);
    }

    private String generateAndSaveActivationToken(User user, String tokenSendingReason) {
        //generate a token
        String generatedToken = generateActivationCode(6);
        var token = VerificationToken.builder()
                .token(generatedToken)
                .createdAt(AppDateUtil.getCurrentUTCLocalDateTime())
                .expiresAt(AppDateUtil.getCurrentUTCLocalDateTime().plus(15, ChronoUnit.MINUTES))
                .user(user)
                .creationReason(tokenSendingReason)
                .build();
        Optional<VerificationToken> existingToken = tokenRepository.findByCreationReasonAndUserEmail(tokenSendingReason, user.getEmail());
        if (existingToken.isPresent()) {
            tokenRepository.delete(existingToken.get());
        }
        tokenRepository.save(token);
        return generatedToken;
    }

    private String generateActivationCode(int length) {
        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(characters.length()); //0--9
            codeBuilder.append(characters.charAt(randomIndex));
        }
        return codeBuilder.toString();
    }

    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request, HttpServletResponse response) throws AccountNotActivatedException, MessagingException, ParseException {
        String requestClientType = requestMetadataContext.getClientType();
        if (!(requestClientType.equals(WEB_CLIENT) || requestClientType.equals(MOBILE_CLIENT))) {
            throw new RuntimeException("Not allowed to do so");
        }
        String encryptedEmail = standardPBEStringEncryptor.encrypt(request.getEmail().toLowerCase());
        User storedUser = userRepository.findByEmailWithActiveRoles(encryptedEmail).orElseThrow(() -> new UsernameNotFoundException("No account with email " + request.getEmail()));
        if (!storedUser.isEnabled()) {
            sendEmail(storedUser, validateEmail);
            throw new AccountNotActivatedException("Please check your email: " + standardPBEStringEncryptor.decrypt(storedUser.getEmail()) + " You need to activate your account before trying to login.");
        }
        if (storedUser.getRequestedAccountClosure()) {
            throw new IllegalStateException("You have closed your Account. Contact us to activate it again.");
        }
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        encryptedEmail,
                        request.getPassword()
                )
        );

        CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();
        String jwtAccessToken = jwtService.generateToken(principal, ACCESS_TOKEN);
        refreshTokenService.deleteExistingUserRefreshTokens(storedUser);
        String refreshToken = refreshTokenService.createRefreshToken(storedUser).getJwtRefreshToken();
        boolean isWebClient = requestClientType.equals(WEB_CLIENT);

        if (isWebClient) {
            ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(isSSLEnabled) //set false for dev only (HTTP)
                    .sameSite(isSSLEnabled ? "None" : "Lax") //"Lax" fine for localhost same-site
                    .path("/")
                    .maxAge(Duration.ofDays(7))
                    .build();

            response.addHeader(SET_COOKIE, refreshCookie.toString());
        }

        AgePolicyResponse agePolicy = storedUser.getDateOfBirth() != null ? agePolicyService.evaluate(storedUser.getDateOfBirth()) : null;
        if (agePolicy != null) {
            //update the last time user logged in to system
            storedUser.setLastLoginAt(Instant.now());
            userRepository.save(storedUser);
        }
        return AuthenticationResponse.builder()
                .jwtToken(jwtAccessToken)
                .jwtRefreshToken(isWebClient ? null : refreshToken)
                .agePolicy(agePolicy)
//                .tokenType("Bearer")
//                .accessTokenExpiresIn(jwtService.getExpirationMs(JwtTokenType.ACCESS) / 1000)
//                .refreshTokenExpiresIn(jwtService.getExpirationMs(JwtTokenType.REFRESH) / 1000)
                .build();
    }

    @Transactional
    public String activateAccount(String token, String userEmail) throws MessagingException {
        //better to include userName in searching token
        VerificationToken savedVerificationToken = tokenRepository.findByTokenAndUserEmail(token, standardPBEStringEncryptor.encrypt(userEmail.toLowerCase()))
                // todo better exception handling needed here
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        if (!savedVerificationToken.getCreationReason().equals(validateEmail)) {
            throw new RuntimeException(savedVerificationToken.getToken() + " is not email validation token");
        }
        if (AppDateUtil.getCurrentUTCLocalDateTime().isAfter(savedVerificationToken.getExpiresAt())) {
            //users need to validate their email with unexpired token/code
            sendEmail(savedVerificationToken.getUser(), validateEmail);
            throw new RuntimeException("Activation token has expired. A new token has been emailed to you");
        }
        User user = savedVerificationToken.getUser();
        user.setEnabled(true);
        savedVerificationToken.setUser(user);
        savedVerificationToken.setValidatedAt(AppDateUtil.getCurrentUTCLocalDateTime());
        tokenRepository.save(savedVerificationToken);
        return "Email Confirmed Successfully";
    }

    public void confirmEmailRequest(String userEmail, String emailReason) throws MessagingException, AccountNotActivatedException {
        boolean isMeetingEmailFormat = PatternUtil.emailAddress(userEmail);
        if (!isMeetingEmailFormat) {
            throw new IllegalArgumentException(userEmail + " is invalid email");
        }
        String requestingEmail = userEmail.toLowerCase();
        String encryptedEmail = standardPBEStringEncryptor.encrypt(requestingEmail);
        User storedUser = userRepository.findByEmail(encryptedEmail).orElseThrow(() -> new UsernameNotFoundException("No user is found with email : " + userEmail));
        if (emailReason.equals(resetPassword)) {
            if (!storedUser.isEnabled()) {
                sendEmail(storedUser, validateEmail);
                throw new AccountNotActivatedException("Please check your email: " + standardPBEStringEncryptor.decrypt(storedUser.getEmail()) + " You need to activate your account before proceeding here.");
            }
        }
        sendEmail(storedUser, emailReason);
    }

    public void resetPassword(AuthenticationRequest request) throws AccountNotActivatedException {
        String encryptedEmail = standardPBEStringEncryptor.encrypt(request.getEmail().toLowerCase());
        User storedUser = userRepository.findByEmail(encryptedEmail).orElseThrow(() -> new UsernameNotFoundException("No account with email " + request.getEmail().toLowerCase()));
        if (storedUser.isAccountLocked()) {
            //here handle a scenario where user is blocked by admin
            throw new AccountNotActivatedException("You need to activate your account before trying to reset password");
        }
        storedUser.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(storedUser);
    }

    public void validatePasswordResetCode(String token) throws MessagingException {
        VerificationToken savedVerificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        if (!savedVerificationToken.getCreationReason().equals(resetPassword)) {
            throw new RuntimeException(savedVerificationToken.getToken() + " is not password reset token");
        }
        if (AppDateUtil.getCurrentUTCLocalDateTime().isAfter(savedVerificationToken.getExpiresAt())) {
            throw new RuntimeException("PasswordReset token has expired. Please try again to reset your password");
        }
        savedVerificationToken.setValidatedAt(AppDateUtil.getCurrentUTCLocalDateTime());
        tokenRepository.save(savedVerificationToken);
    }

    @Transactional
    public void deleteAccount(String email, Principal principal) {
        if (!standardPBEStringEncryptor.decrypt(principal.getName()).equals(email)) {
            throw new RuntimeException("Operation denied");
        }
        User savedUser = userRepository.findByEmail(principal.getName()).orElseThrow(() -> new RuntimeException("Account not found"));
        List<VerificationToken> userVerificationTokenList = tokenRepository.findByUserEmail(principal.getName());
        refreshTokenRepository.deleteByUserEmail(principal.getName());
        tokenRepository.deleteAll(userVerificationTokenList);
        savedUser.setRequestedAccountClosure(true);
        savedUser.setLastModifiedDate(Instant.now());
        savedUser.setAccountModifiedBy(savedUser.getUserName().toLowerCase());
        userRepository.save(savedUser);
        //
//        SecurityContextHolder.getContext().setAuthentication(null);
    }

    public void validateAccountDeleteCode(String token, Principal principal) {
        String authName = principal.getName();
        VerificationToken savedVerificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        User user = savedVerificationToken.getUser();
        if (!user.getEmail().equals(authName)) {
            throw new RuntimeException("Operation denied");
        }
        if (!savedVerificationToken.getCreationReason().equals(deleteAccountEmail)) {
            throw new RuntimeException(savedVerificationToken.getToken() + " is not delete  account token");
        }
        if (AppDateUtil.getCurrentUTCLocalDateTime().isAfter(savedVerificationToken.getExpiresAt())) {
            throw new RuntimeException("Account Delete token has been already expired.");
        }
        savedVerificationToken.setValidatedAt(AppDateUtil.getCurrentUTCLocalDateTime());
        tokenRepository.save(savedVerificationToken);
    }

    public boolean validateAppConfigCode(String token, Principal principal) {
        String authName = principal.getName();
        VerificationToken savedVerificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        User user = savedVerificationToken.getUser();
        if (!user.getEmail().equals(authName)) {
            throw new RuntimeException("Operation denied");
        }
        if (!savedVerificationToken.getCreationReason().equals(appConfigKey)) {
            throw new RuntimeException(savedVerificationToken.getToken() + " is not an app config token");
        }
        if (AppDateUtil.getCurrentUTCLocalDateTime().isAfter(savedVerificationToken.getExpiresAt())) {
            throw new RuntimeException("App Config token has been already expired.");
        }
        savedVerificationToken.setValidatedAt(AppDateUtil.getCurrentUTCLocalDateTime());
        tokenRepository.save(savedVerificationToken);
        return true;
    }

    public ResponseEntity<?> getJwtRefreshToken(HttpServletRequest request, HttpServletResponse response) throws ParseException {
        String clientType = requestMetadataContext.getClientType();
        if (!(clientType.equals(WEB_CLIENT) || clientType.equals(MOBILE_CLIENT))) {
            throw new RuntimeException("Not allowed to do so");
        }
        String refreshToken = null;
        boolean isWebClient =
                clientType.equals(WEB_CLIENT);

        // Web: refresh token from cookie
        if (isWebClient && request.getCookies() != null) {
            refreshToken = getCookieNameValueFromRequest("refreshToken", request);
        }

        // Mobile: currently from Authorization header
        if (!isWebClient && refreshToken == null) {
            String headerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (headerToken != null && headerToken.startsWith("Bearer ")) {
                refreshToken = headerToken.substring(7);
            }
        }

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Refresh token missing"));
        }

        RefreshToken storedRefreshToken = refreshTokenService.findByToken(refreshToken);
        if (storedRefreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid refresh token"));
        }

        User storedUser = storedRefreshToken.getUser();
        CustomUserDetails customUserDetails = new CustomUserDetails(storedUser);

        boolean isRefreshTokenValid =
                jwtService.isTokenValid(refreshToken, customUserDetails, REFRESH_TOKEN);

        if (!isRefreshTokenValid) {
            refreshTokenService.deleteByToken(refreshToken);
            clearRefreshCookie(response);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid refresh token"));
        }

        // Rotate refresh token:
        // 1. create new access token
        // 2. create new refresh token
        // 3. delete old refresh token
        String newAccessToken = jwtService.generateToken(customUserDetails, ACCESS_TOKEN);
        refreshTokenService.deleteExistingUserRefreshTokens(storedUser);
        String newRefreshToken = refreshTokenService.createRefreshToken(storedUser).getJwtRefreshToken();

//        refreshTokenService.deleteByToken(refreshToken);

        // Web: set rotated refresh token in cookie
        if (isWebClient) {
            addRefreshCookie(response, newRefreshToken);
        }
        return ResponseEntity.ok(
                AuthenticationResponse.builder()
                        .jwtToken(newAccessToken)
                        .jwtRefreshToken(isWebClient ? null : newRefreshToken)
                        .build()
        );
    }
    private void addRefreshCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(isSSLEnabled)
                .sameSite(isSSLEnabled ? "None" : "Lax")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response,
                                    Authentication authentication) throws ParseException {
        clearRefreshCookie(response);
        if (authentication != null && authentication.isAuthenticated()) {
            String userEmail = authentication.getName();
            User storedUser = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "Account not found"
                    ));

//            refreshTokenService.deleteAllByUser(storedUser);
//            refreshTokenService.deleteByUserEmail(storedUser.getEmail());
            refreshTokenService.deleteExistingUserRefreshTokens(storedUser);
        }

        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie clearCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(isSSLEnabled)
                .sameSite(isSSLEnabled ? "None" : "Lax")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(SET_COOKIE, clearCookie.toString());
    }

    private static String getCookieNameValueFromRequest(String cookieName, HttpServletRequest request) {
        String refreshToken;
        refreshToken = Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals(cookieName))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
        return refreshToken;
    }

    public boolean doesUserExist(String userIdentity) {
        User storedUser;
        if (userIdentity.contains("@")) {
            storedUser = userRepository.findByEmail(standardPBEStringEncryptor.encrypt(userIdentity.toLowerCase())).orElseThrow(() -> new RuntimeException("Account not found with the provided information=> " + userIdentity));
        } else {
            storedUser = userRepository.findByUserName(userIdentity.toLowerCase()).orElseThrow(() -> new RuntimeException("Account not found with the provided information=> " + userIdentity));
        }
        return storedUser == null ? false : true;
    }

    public User existingAppUser(String userIdentity) {
        User storedUser;
        if (userIdentity.contains("@")) {
            storedUser = userRepository.findByEmail(standardPBEStringEncryptor.encrypt(userIdentity.toLowerCase())).orElseThrow(() -> new RuntimeException("Account not found with the provided information=> " + userIdentity));
        } else {
            storedUser = userRepository.findByUserName(userIdentity.toLowerCase()).orElseThrow(() -> new RuntimeException("Account not found with the provided information=> " + userIdentity));
        }
        return storedUser;
    }
}
