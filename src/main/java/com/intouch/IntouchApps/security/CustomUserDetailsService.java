package com.intouch.IntouchApps.security;

import com.intouch.IntouchApps.user.User;
import com.intouch.IntouchApps.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
@Primary
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
    User user = userRepository.findByEmailWithActiveRoles(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("No account found with email: " + userEmail));
        return new CustomUserDetails(user);
    }
}
