package com.intouch.IntouchApps.auth;

import com.intouch.IntouchApps.handler.AccountNotActivatedException;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("controlledAuth")//context-path already set
@RequiredArgsConstructor
public class SecuredAuthenticationController {
    private final AuthenticationService service;
    @GetMapping("/validate-account-delete-code")
    public void validateAccountDeleteCode(@RequestParam("token") String token, Principal principal){
        service.validateAccountDeleteCode(token, principal);
    }
    @PostMapping("/deleteAccount")
    public void deleteAccount(@RequestHeader("AppUserEmail") String email, Principal principal) {
        service.deleteAccount(email, principal);
    }
}
