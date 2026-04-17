package com.intouch.IntouchApps.security;

import com.intouch.IntouchApps.enums.JwtTokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.text.ParseException;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

@Service
@RefreshScope
@Transactional
public class JwtService {
    private final SecretKey accessSigningKey;
    private final SecretKey refreshSigningKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtService(
            @Value("${application.security.jwt.secret_key}") String accessSecret,
            @Value("${application.security.jwt.refresh_token.secret_key}") String refreshSecret,
            @Value("${application.security.jwt.expiration}") long accessTokenExpirationMs,
            @Value("${application.security.jwt.refresh_token.expiration}") long refreshTokenExpirationMs
    ) {
        this.accessSigningKey = Keys.hmacShaKeyFor(accessSecret.getBytes(StandardCharsets.UTF_8));
        this.refreshSigningKey = Keys.hmacShaKeyFor(refreshSecret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String generateToken(CustomUserDetails userDetails, JwtTokenType tokenType) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(getExpirationMs(tokenType));
        Map<String, Object> claims = new HashMap<>();
//        claims.put("userId", userDetails.getUserId());
        claims.put("fullName", userDetails.getFullName());
        claims.put("email", userDetails.getEmail());
        claims.put("tokenType", tokenType.name());
        if (tokenType == JwtTokenType.ACCESS) {
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
            claims.put("userName", userDetails.getDisplayUsername());
            claims.put("roles", roles);
        }

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername()) // user email
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey(tokenType))
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails, JwtTokenType tokenType) {
        final String username = extractUsername(token, tokenType);
        final String storedType = extractTokenType(token, tokenType);
        return username.equals(userDetails.getUsername())
                && tokenType.name().equals(storedType)
                && !isTokenExpired(token, tokenType);
    }

    public String extractUsername(String token, JwtTokenType tokenType) {
        return extractClaim(token, Claims::getSubject, tokenType);
    }

    private String normalizeToken(String token) {
        if (token == null) throw new JwtException("Missing token");
        token = token.trim();
        // remove accidental template braces
        if (token.startsWith("{{") && token.endsWith("}}")) {
            token = token.substring(2, token.length() - 2).trim();
        }
        // hard reject any remaining braces
        if (token.indexOf('{') >= 0 || token.indexOf('}') >= 0) {
            throw new JwtException("Malformed token (contains braces)");
        }
        return token;
    }

    public String extractTokenType(String token, JwtTokenType tokenType) {
        return extractAllClaims(token, tokenType).get("tokenType", String.class);
    }

    public Long extractUserId(String token, JwtTokenType tokenType) {
        return extractAllClaims(token, tokenType).get("userId", Long.class);
    }

    public List<String> extractRoles(String token, JwtTokenType tokenType) {
        List<?> raw = extractAllClaims(token, tokenType).get("roles", List.class);
        if (raw == null) {
            return List.of();
        }
        return raw.stream().map(String::valueOf).toList();
    }

    public long getExpirationMs(JwtTokenType tokenType) {
        return tokenType == JwtTokenType.ACCESS
                ? accessTokenExpirationMs
                : refreshTokenExpirationMs;
    }

    private boolean isTokenExpired(String token, JwtTokenType tokenType) {
        return extractClaim(token, Claims::getExpiration, tokenType).before(new Date());
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver, JwtTokenType tokenType) {
        Claims claims = extractAllClaims(token, tokenType);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token, JwtTokenType tokenType) {
        token = normalizeToken(token);
        return Jwts.parser()
                .verifyWith(getSigningKey(tokenType))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey(JwtTokenType tokenType) {
        return tokenType == JwtTokenType.ACCESS ? accessSigningKey : refreshSigningKey;
    }
}
