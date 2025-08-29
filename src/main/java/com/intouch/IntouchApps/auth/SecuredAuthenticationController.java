package com.intouch.IntouchApps.auth;

import com.intouch.IntouchApps.handler.AccountNotActivatedException;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("loggedUser")//context-path already set
@RequiredArgsConstructor
public class SecuredAuthenticationController {
    private final AuthenticationService service;
    private final StandardPBEStringEncryptor standardPBEStringEncryptor;
    @GetMapping("/validate-account-delete-code")
    public void validateAccountDeleteCode(@RequestParam("token") String token, Principal principal){
        service.validateAccountDeleteCode(token, principal);
    }
    @PostMapping("/deleteAccount")
    public void deleteAccount(@RequestHeader("AppUserEmail") String email, Principal principal) {
        service.deleteAccount(email, principal);
    }
    @GetMapping("/sendCodeToUserEmail")
    public void requestConfirmation(Principal principal,
            @RequestParam String emailReason
    ) throws MessagingException, AccountNotActivatedException {
        service.confirmEmailRequest(standardPBEStringEncryptor.decrypt(principal.getName()), emailReason);
    }
}
