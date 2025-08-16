package com.intouch.IntouchApps.user;

import com.intouch.IntouchApps.security.JwtService;
import com.intouch.IntouchApps.utils.AppDateUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final StandardPBEStringEncryptor standardPBEStringEncryptor;
    @Value("${application.security.jwt.refresh_token.expiration}")
    private Long refreshTokenDurationMs;

    public RefreshToken createRefreshToken(User user) throws ParseException {
        Map claims = new HashMap();
        claims.put("fullName", user.fullName());
        if(user.isAccountLocked() || !user.isEnabled()){
            throw new RuntimeException("Some Thing went wrong");
        }
        String token = jwtService.generateToken(claims, user, true);
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .jwtRefreshToken(token)
                .expiresAt(AppDateUtil.getCurrentUTCLocalDateTime().plusNanos(refreshTokenDurationMs*1000000))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }
    public RefreshToken findByToken(String token) throws ParseException {
        RefreshToken storedRefreshToken = refreshTokenRepository.findByJwtRefreshToken(token).orElseThrow(() -> new AccessDeniedException("Authentication Failed"));
        boolean isTokenValid = storedRefreshToken.getExpiresAt().isAfter(AppDateUtil.getCurrentUTCLocalDateTime()) && jwtService.isTokenValid(storedRefreshToken.getJwtRefreshToken(), storedRefreshToken.getUser(), true);
        if(isTokenValid){
            return storedRefreshToken;
        }else{
            throw new BadCredentialsException("You need to login again");
        }
    }
    public void deleteByToken(String token) throws ParseException {

        RefreshToken storedRefreshToken = refreshTokenRepository.findByJwtRefreshToken(token).orElseThrow(() -> new AccessDeniedException("Authentication Failed"));
        refreshTokenRepository.delete(storedRefreshToken);
    }
    public List<RefreshToken> findByUserEmail(String userEmail) {
        return refreshTokenRepository.findByUserEmail(userEmail);
    }
    public void revokeToken(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }
    public void deleteByUserEmail(String userEmail) {
        refreshTokenRepository.deleteByUserEmail(userEmail);
    }
    public boolean isTokenValid(RefreshToken token) {
        return !token.isRevoked() && token.getExpiresAt().isAfter(AppDateUtil.getCurrentUTCLocalDateTime());
    }
    public void deleteExistingUserRefreshTokens(List<RefreshToken> storedRefreshTokens) {
        refreshTokenRepository.deleteAll(storedRefreshTokens);
    }
}
