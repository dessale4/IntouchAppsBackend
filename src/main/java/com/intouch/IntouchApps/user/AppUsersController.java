package com.intouch.IntouchApps.user;

import com.intouch.IntouchApps.auth.AuthenticationService;
import com.intouch.IntouchApps.handler.AccountNotActivatedException;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("appUsers")
public class AppUsersController {
    private final AuthenticationService service;
    private final StandardPBEStringEncryptor standardPBEStringEncryptor;
    private final AppUsersService appUsersService;
    @GetMapping("/userNames")
    public List<AccountDTO> getUserNames(){
        return appUsersService.getAppUsernames();
    }
    @GetMapping("/loggedUser")
    public ResponseEntity<?> fetchUser(HttpServletRequest request, Principal principal) {
        UserDTO user = appUsersService.loadUserByUsername(request, principal);// from cookie
        return ResponseEntity.ok(user);
    }
    @PatchMapping("/updateUserScreenLockPin")
    public ResponseEntity<?> updateUserScreenLockPin(Principal principal, @RequestParam Integer screenLockPin) {
        Integer userScreenLockPin = appUsersService.updateUserScreenLockPin(principal, screenLockPin);
        return ResponseEntity.ok(userScreenLockPin);
    }
    @GetMapping("/sendCodeToUserEmail")
    public void requestEmailConfirmation(Principal principal,
                                         @RequestParam String emailReason
    ) throws MessagingException, AccountNotActivatedException {
        service.confirmEmailRequest(standardPBEStringEncryptor.decrypt(principal.getName()), emailReason);
    }
    @GetMapping("/validate-screen-lock-pin-code")
    public void validateScreenLockPinCode(@RequestParam("token") String token, Principal principal, @RequestParam String emailReason){
        appUsersService.validateUserEmailedToken(token, principal, emailReason);
    }
}
