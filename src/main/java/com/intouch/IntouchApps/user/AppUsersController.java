package com.intouch.IntouchApps.user;

import com.intouch.IntouchApps.auth.AuthenticationService;
import com.intouch.IntouchApps.handler.AccountNotActivatedException;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.text.ParseException;
import java.time.LocalDate;
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
    public ResponseEntity<?> fetchUser(HttpServletRequest request) {
        UserDTO user = appUsersService.loadLoggedUserByUserEmail(request);// from cookie
        return ResponseEntity.ok(user);
    }
    @PostMapping("/updateBirthDate")
    public ResponseEntity<?> updateUserBirthDay(@Valid @RequestBody UpdateDobRequest request, @RequestParam(value = "phoneNumber", required = false) String phoneNumber){
        LocalDate dateOfBirth = request.dateOfBirth();
        AgePolicyResponse agePolicyResponse =  appUsersService.updateUserBirthDay(dateOfBirth, phoneNumber);
        return ResponseEntity.ok(agePolicyResponse);
    }
    @PatchMapping("/updateUserScreenLockPin")
    public ResponseEntity<?> updateUserScreenLockPin(@RequestParam Integer screenLockPin) {
        Integer userScreenLockPin = appUsersService.updateUserScreenLockPin(screenLockPin);
        return ResponseEntity.ok(userScreenLockPin);
    }
    @GetMapping("/sendCodeToUserEmail")
    public void requestEmailConfirmation(Principal principal,
                                         @RequestParam String emailReason
    ) throws MessagingException, AccountNotActivatedException {
        service.confirmEmailRequest(standardPBEStringEncryptor.decrypt(principal.getName()), emailReason);
    }
    @GetMapping("/validate-screen-lock-pin-code")
    public void validateScreenLockPinCode(@RequestParam("token") String token, @RequestParam String emailReason){
        appUsersService.validateUserEmailedToken(token, emailReason);
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ParseException {
        return service.logout(request, response, authentication);
    }
}
