package com.intouch.IntouchApps.auth;

import com.intouch.IntouchApps.handler.AccountNotActivatedException;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("auth")//context-path already set
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthenticationController {
    private final AuthenticationService service;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> register(
            @RequestBody @Valid RegistrationRequest request
    ) throws MessagingException {
        service.register(request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody @Valid AuthenticationRequest request
            , HttpServletResponse response
    ) throws AccountNotActivatedException, MessagingException, ParseException {
        return ResponseEntity.ok(service.authenticate(request, response));
    }
    @GetMapping("/activate-account")
    public String confirmEmail(
            @RequestParam String token,  @RequestParam String userEmail
    ) throws MessagingException {
       return service.activateAccount(token, userEmail);
    }
    @GetMapping("/sendCodeWithEmail")
    public void requestConfirmation(
            @RequestParam String userEmail,
            @RequestParam String emailReason
    ) throws MessagingException, AccountNotActivatedException {
       service.confirmEmailRequest(userEmail, emailReason);
    }
    @PostMapping("/resetPassword")
    public void resetPassword(
            @RequestBody @Valid AuthenticationRequest request
    ) throws AccountNotActivatedException {
        service.resetPassword(request);
    }

    @GetMapping("/validate-passwordReset-code")
    public void validatePasswordResetCode(
            @RequestParam String token
    ) throws MessagingException {
        service.validatePasswordResetCode(token);
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) throws ParseException {
        return service.logout(request, response);
    }
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) throws ParseException {
         return ResponseEntity.ok( service.getJwtRefreshToken(request, response));
        }
}
