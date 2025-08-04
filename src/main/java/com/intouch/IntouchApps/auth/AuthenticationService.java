package com.intouch.IntouchApps.auth;

import com.intouch.IntouchApps.email.AppEmail;
import com.intouch.IntouchApps.email.EmailService;
import com.intouch.IntouchApps.handler.AccountNotActivatedException;
import com.intouch.IntouchApps.role.RoleRepository;
import com.intouch.IntouchApps.security.JwtService;
//import com.intouch.IntouchApps.security.StringEncryptConverter;
import com.intouch.IntouchApps.user.*;
import com.intouch.IntouchApps.utils.AppDateUtil;
import com.intouch.IntouchApps.utils.PatternUtil;
import com.intouch.IntouchApps.utils.UserAndRolesUtil;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.intouch.IntouchApps.email.EmailTemplateName.GENERIC_EMAIL_TEMPLATE;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final StandardPBEStringEncryptor standardPBEStringEncryptor;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    @Value("${application.mailing.backend.email_validation_key}")
    private String validateEmail;
    @Value("${application.mailing.backend.account_delete_key}")
    private String deleteAccountEmail;
    @Value("${application.mailing.backend.password_reset_key}")
    private String resetPassword;
    @Value("${application.mailing.frontend.activation_url}")
    private String activationUrl;
    public void register(RegistrationRequest request) throws MessagingException {
        var userRole = roleRepository.findByName("USER")
                //todo - apply best exception handling approach
                .orElseThrow(()->new IllegalStateException("ROLE USER was not initialized"));
        String encryptedEmail = standardPBEStringEncryptor.encrypt(request.getEmail().toLowerCase());

        var user = User.builder()
                        .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(encryptedEmail)
                .publicUserName(request.getUserName().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .accountLocked(false)
                .enabled(false)
                .roles(List.of(userRole))
                .createdDate(AppDateUtil.getCurrentUTCLocalDateTime())
                .lastModifiedDate(AppDateUtil.getCurrentUTCLocalDateTime())
                .build();
        user = userRepository.save(user);
        sendValidationEmail(user, validateEmail);
    }
    //user is as is fetched from DB
    private void sendValidationEmail(User user, String validationReason) throws MessagingException {
        var newToken = generateAndSaveActivationToken(user, validationReason);
        String decryptedEmail = standardPBEStringEncryptor.decrypt(user.getEmail());

        boolean isEmailAdminSetEmail = UserAndRolesUtil.adminSetEmails.contains(decryptedEmail);
        String verifyingEmail = isEmailAdminSetEmail ? UserAndRolesUtil.adminSetVerifyingEmail.toLowerCase() :decryptedEmail;

        // send email
        AppEmail appEmail = AppEmail.builder()
                .to(verifyingEmail)
                .username(user.fullName())
                .emailTemplate(GENERIC_EMAIL_TEMPLATE)
                .confirmationUrl(activationUrl)
                .activationCode(newToken)
                .subject(validationReason.toUpperCase())
                .messageTitle(validationReason.toUpperCase())
                .confirmationText(validationReason.toUpperCase())
                .message(validationReason.toUpperCase())
                .build();

            emailService.sendEmail( appEmail );

    }
    private String generateAndSaveActivationToken(User user, String tokenSendingReason) {
        //generate a token
        String generatedToken = generateActivationCode(6);
        var token  = Token.builder()
                .token(generatedToken)
                .createdAt(AppDateUtil.getCurrentUTCLocalDateTime())
                .expiresAt(AppDateUtil.getCurrentUTCLocalDateTime().plusMinutes(15))
                .user(user)
                .creationReason(tokenSendingReason)
                .build();
        Optional<Token> existingToken = tokenRepository.findByCreationReasonAndUserEmail(tokenSendingReason, user.getEmail());
        if(existingToken.isPresent()){
            tokenRepository.delete(existingToken.get());
        }
        tokenRepository.save(token);
        return generatedToken;
    }

    private String generateActivationCode(int length) {
        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();
        for(int i = 0; i<length;i++){
            int randomIndex = secureRandom.nextInt(characters.length()); //0--9
            codeBuilder.append(characters.charAt(randomIndex));
        }
        return codeBuilder.toString();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) throws AccountNotActivatedException, MessagingException, ParseException {
        String encryptedEmail = standardPBEStringEncryptor.encrypt(request.getEmail().toLowerCase());
        User storedUser  = userRepository.findByEmail(encryptedEmail).orElseThrow(()->new UsernameNotFoundException("No account with email " + request.getEmail()));
        if(!storedUser.isEnabled()){
            sendValidationEmail(storedUser, validateEmail);
            throw new AccountNotActivatedException("Please check your email: " +standardPBEStringEncryptor.decrypt(storedUser.getEmail())  + " You need to activate your account before trying to login");
        }
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        encryptedEmail,
                        request.getPassword()
                )
        );
        var claims = new HashMap<String, Object>();

        var user = (User)auth.getPrincipal();//take care
        boolean subscriptionIsExpired = user.getSubscriptionEndDate() !=null && AppDateUtil.getCurrentUTCLocalDateTime().isAfter(user.getSubscriptionEndDate());
        claims.put("fullName", user.fullName());
        var jwtToken = jwtService.generateToken(claims, user);
        System.out.println("GGGGGG");
        System.out.println(auth.getAuthorities());
        System.out.println("========DDDD");

        System.out.println(user.getRoles());
        System.out.println("========AAA");
        UserDTO userDTO = UserDTO.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(request.getEmail())
                .username(user.getPublicUserName())
                .enabled(user.isEnabled())
                .isSubscribed(subscriptionIsExpired ? false: user.isSubscribed())
                .accountLocked(user.isAccountLocked())
                .roles(user.getRoles())
                .subscriptionEndDate(user.getSubscriptionEndDate())
                .build();
        return AuthenticationResponse.builder()
                .jwtToken(jwtToken)
                .userDTO(userDTO)
                .build();
    }
//    @Transactional
    public String activateAccount(String token, String userEmail) throws MessagingException {
        //better to include username in searching token
        Token savedToken = tokenRepository.findByTokenAndUserEmail(token,standardPBEStringEncryptor.encrypt(userEmail.toLowerCase()))
                // todo better exception handling needed here
                .orElseThrow(()-> new RuntimeException("Invalid token"));
        if(!savedToken.getCreationReason().equals(validateEmail)){
            throw new RuntimeException(savedToken.getToken() + " is not email validation token");
        }
        if(AppDateUtil.getCurrentUTCLocalDateTime().isAfter(savedToken.getExpiresAt())){
            //check reason
            sendValidationEmail(savedToken.getUser(), validateEmail);
            throw new RuntimeException("Activation token has expired. A new token has been emailed to you");
        }
        User user = savedToken.getUser();
        user.setEnabled(true);
        savedToken.setUser(user);
        savedToken.setValidatedAt(AppDateUtil.getCurrentUTCLocalDateTime());
        tokenRepository.save(savedToken);
        return "Email Confirmed Successfully";
    }

    public void confirmEmailRequest(String userEmail, String emailReason) throws MessagingException, AccountNotActivatedException {
        boolean isMeetingEmailFormat = PatternUtil.emailAddress(userEmail);
        if(!isMeetingEmailFormat){
            throw new IllegalArgumentException(userEmail + " is invalid email");
        }
        String requestingEmail = userEmail.toLowerCase();
//        boolean isEmailAdminSetEmail = RolesUtil.adminSetEmails.contains(requestingEmail);
//        String encryptedEmail = isEmailAdminSetEmail ? standardPBEStringEncryptor.encrypt(RolesUtil.adminSetVerifyingEmail.toLowerCase()) :standardPBEStringEncryptor.encrypt(requestingEmail);
        String encryptedEmail = standardPBEStringEncryptor.encrypt(requestingEmail);

        User storedUser = userRepository.findByEmail(encryptedEmail).orElseThrow(()-> new UsernameNotFoundException("No user is found with email : "+ userEmail));

        if(emailReason.equals(resetPassword)){
            if(!storedUser.isEnabled()){
                sendValidationEmail(storedUser, validateEmail);
                throw new AccountNotActivatedException("Please check your email: "+ standardPBEStringEncryptor.decrypt(storedUser.getEmail())  + " You need to activate your account before trying to request a code");
            }
            sendPasswordResetToken(storedUser);
        }
        if(emailReason.equals(validateEmail)){
            sendValidationEmail(storedUser, emailReason);
        }
        if(emailReason.equals(deleteAccountEmail)){
            sendValidationEmail(storedUser, emailReason);
        }
    }
    private void sendPasswordResetToken(User storedUser) throws MessagingException {
        String passwordResetToken = generateAndSaveActivationToken(storedUser, resetPassword);
        String decryptedEmail = standardPBEStringEncryptor.decrypt(storedUser.getEmail());
        AppEmail appEmail = AppEmail.builder()
                .to(decryptedEmail)
                .username(storedUser.fullName())
                .emailTemplate(GENERIC_EMAIL_TEMPLATE)
                .confirmationUrl(activationUrl)
                .activationCode(passwordResetToken)
                .subject("Change Password")
                .messageTitle("Reset Password")
                .confirmationText("Reset your password")
                .message("change your password:")
                .build();

        emailService.sendEmail( appEmail );

    }
    public void resetPassword(AuthenticationRequest request) throws AccountNotActivatedException {

        String encryptedEmail = standardPBEStringEncryptor.encrypt(request.getEmail().toLowerCase());

        User storedUser  = userRepository.findByEmail(encryptedEmail).orElseThrow(()->new UsernameNotFoundException("No account with email " + request.getEmail().toLowerCase()));
        if(storedUser.isAccountLocked()){
            //here handle a scenario where user is blocked by admin
            throw new AccountNotActivatedException("You need to activate your account before trying to reset password");
        }

        storedUser.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(storedUser);

    }

    public void validatePasswordResetCode(String token) throws MessagingException {
        System.out.println("Received Token: " + token);
        Token savedToken = tokenRepository.findByToken(token)
               .orElseThrow(()-> new RuntimeException("Invalid token"));
        if(!savedToken.getCreationReason().equals(resetPassword)){
            throw new RuntimeException(savedToken.getToken() + " is not password reset token");
        }
        if(AppDateUtil.getCurrentUTCLocalDateTime().isAfter(savedToken.getExpiresAt())){
          throw new RuntimeException("PasswordReset token has expired. Please try again to reset your password");
        }
        savedToken.setValidatedAt(AppDateUtil.getCurrentUTCLocalDateTime());
        tokenRepository.save(savedToken);
    }

    public void deleteAccount(String email, Principal principal) {
        if(!standardPBEStringEncryptor.decrypt(principal.getName()).equals(email)){
            throw new RuntimeException("Operation denied");
        }
        User savedUser = userRepository.findByEmail(principal.getName()).orElseThrow(() -> new RuntimeException("Account not found"));
        List<Token> userTokenList = tokenRepository.findByUserEmail(principal.getName());
        tokenRepository.deleteAll(userTokenList);
        userRepository.delete(savedUser);
    }

    public void validateAccountDeleteCode(String token, Principal principal) {
        String authName = principal.getName();
        Token savedToken = tokenRepository.findByToken(token)
               .orElseThrow(()-> new RuntimeException("Invalid token"));
        User user = savedToken.getUser();
        if(!user.getEmail().equals(authName)){
            throw new RuntimeException("Operation denied");
        }
        if(!savedToken.getCreationReason().equals(deleteAccountEmail)){
            throw new RuntimeException(savedToken.getToken() + " is not delete  account token");
        }
        if(AppDateUtil.getCurrentUTCLocalDateTime().isAfter(savedToken.getExpiresAt())){
                 throw new RuntimeException("Account Delete token has been already expired.");
        }
        savedToken.setValidatedAt(AppDateUtil.getCurrentUTCLocalDateTime());
        tokenRepository.save(savedToken);
    }
}
