package com.intouch.IntouchApps.user;

import com.intouch.IntouchApps.enums.JwtTokenType;
import com.intouch.IntouchApps.security.CustomUserDetails;
import com.intouch.IntouchApps.security.JwtService;
import com.intouch.IntouchApps.utils.AppDateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@RefreshScope
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final StandardPBEStringEncryptor standardPBEStringEncryptor;
    @Value("${application.security.jwt.refresh_token.expiration}")
    private Long refreshTokenDurationMs;

    public RefreshToken createRefreshToken(User user) throws ParseException {
        if (user.isAccountLocked() || !user.isEnabled()) {
            throw new RuntimeException("Some Thing went wrong");
        }
        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        String newRefreshToken = jwtService.generateToken(customUserDetails, JwtTokenType.REFRESH);
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .jwtRefreshToken(newRefreshToken)
                .expiresAt(AppDateUtil.getCurrentUTCLocalDateTime().plusNanos(refreshTokenDurationMs * 1000000))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken findByToken(String token) throws ParseException {
        RefreshToken storedRefreshToken = refreshTokenRepository.findByJwtRefreshToken(token).orElseThrow(() -> new AccessDeniedException("Authentication Failed"));
        CustomUserDetails customUserDetails = new CustomUserDetails(storedRefreshToken.getUser());
        return storedRefreshToken;

    }

    @Transactional
    public void deleteByToken(String token) throws ParseException {

        RefreshToken storedRefreshToken = refreshTokenRepository.findByJwtRefreshToken(token).orElseThrow(() -> new AccessDeniedException("Authentication Failed"));
        refreshTokenRepository.delete(storedRefreshToken);
        refreshTokenRepository.flush();
    }

    public List<RefreshToken> findByUserEmail(String userEmail) {
        return refreshTokenRepository.findByUserEmail(userEmail);
    }

    public void revokeToken(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    @Transactional
    public void deleteByUserEmail(String userEmail) {

        refreshTokenRepository.deleteByUserEmail(userEmail);
        refreshTokenRepository.flush();
    }

    public boolean isTokenValid(RefreshToken token) {
        return !token.isRevoked() && token.getExpiresAt().isAfter(AppDateUtil.getCurrentUTCLocalDateTime());
    }


    @Transactional
    public void deleteExistingUserRefreshTokens(User user) {
        refreshTokenRepository.deleteByUserId(user.getId());
        refreshTokenRepository.flush();
    }

}
