package com.intouch.IntouchApps.user;

import com.intouch.IntouchApps.security.JwtService;
import com.intouch.IntouchApps.utils.AppDateUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppUsersService {
    private final UserRepository userRepository;
    private final StandardPBEStringEncryptor standardPBEStringEncryptor;
    private final JwtService jwtService;
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

    public UserDTO loadUserByUsername(HttpServletRequest request) {
        String jwtToken = null;
        if (request.getCookies() != null) {
            jwtToken = Arrays.stream(request.getCookies())
                    .filter(c -> c.getName().equals("jwt"))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        }
        if(jwtToken == null){
            String authHeader = request.getHeader(AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwtToken = authHeader.substring(7);
            }
        }
        String userEmail = jwtService.extractUsername(jwtToken, false);
        String decryptedEmail = standardPBEStringEncryptor.decrypt(userEmail);
        User user = userRepository.findByEmail(userEmail).orElseThrow(() -> new UsernameNotFoundException("No user was found with email: " + decryptedEmail));
//        ====== consider returning UserDTO instead of UserDetails======
        boolean subscriptionIsExpired = user.getSubscriptionEndDate() !=null && AppDateUtil.getCurrentUTCLocalDateTime().isAfter(user.getSubscriptionEndDate());
        UserDTO userDTO = UserDTO.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(standardPBEStringEncryptor.decrypt(userEmail))
                .username(user.getPublicUserName())
                .enabled(user.isEnabled())
                .isSubscribed(subscriptionIsExpired ? false: user.isSubscribed())
                .accountLocked(user.isAccountLocked())
                .roles(user.getRoles())
                .subscriptionEndDate(user.getSubscriptionEndDate())
                .build();
        return userDTO;
    }
}

