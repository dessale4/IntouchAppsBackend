package com.intouch.IntouchApps.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${application.security.jwt.secret_key}")
    private String secretKey;
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;
    @Value("${application.security.jwt.refresh_token.secret_key}")
    private String refreshTokenSecretKey;
    @Value("${application.security.jwt.refresh_token.expiration}")
    private long refreshTokenExpiration;

    public String extractUsername(String token, boolean isRefreshJwtToken) {

        return extractClaim(token, Claims::getSubject, isRefreshJwtToken);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimResolver, boolean isRefreshJwtToken) {
        final Claims claims = extractAllClaims(token, isRefreshJwtToken);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token, boolean isRefreshJwtToken) {
        Key signInKey = getSignInKey(isRefreshJwtToken);
        return Jwts.parser()
                .setSigningKey(signInKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String generateToken(UserDetails userDetails, boolean isRefreshJwtToken) throws ParseException {
        return generateToken(new HashMap<>(), userDetails, isRefreshJwtToken);
    }
    public String generateToken(Map<String, Object> claims, UserDetails userDetails, boolean isRefreshJwtToken) throws ParseException {
//        System.out.println("generateToken called");
        return buildToken(claims, userDetails
                , isRefreshJwtToken
        );
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            boolean isRefreshJwtToken) throws ParseException {
        var authorities = userDetails
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        long tokenExpiration = isRefreshJwtToken ? refreshTokenExpiration : jwtExpiration;
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                //recheck Date obj and CurrentUTCDateTime scenario (occurred in 3 places)
                .issuedAt(new Date(System.currentTimeMillis()))
//                .issuedAt(new Date(AppDateUtil.getCurrentUTCTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + tokenExpiration))
//                .setExpiration(new Date(AppDateUtil.getCurrentUTCTimeMillis() + tokenExpiration))
                .claim("authorities", authorities) //add claims
                .signWith(getSignInKey(isRefreshJwtToken))
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails, boolean isRefreshJwtToken) throws ParseException {
        final String username = extractUsername(token, isRefreshJwtToken);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token, isRefreshJwtToken));
    }

    private boolean isTokenExpired(String token, boolean isRefreshJwtToken) throws ParseException {
        Date expirationDate = extractExpiration(token, isRefreshJwtToken);
        Date dateNow = new Date();
        return expirationDate.before(dateNow);
//        return extractExpiration(token).before(new Date());
//        return extractExpiration(token).before(AppDateUtil.getDateFromCurrentUTCDateTime());
    }

    private Date extractExpiration(String token, boolean isRefreshJwtToken) {
        return extractClaim(token, Claims::getExpiration, isRefreshJwtToken);
    }

    private Key getSignInKey(boolean isRefreshJwtToken) {
        String jwtSecreteKey = isRefreshJwtToken ? refreshTokenSecretKey : secretKey;
//        System.out.println("isRefreshJwtToken : jwtSecreteKey =>" + isRefreshJwtToken + " : " + jwtSecreteKey);
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecreteKey);
        // Print the raw key bytes (e.g., Base64 encoded)
//        String encodedKeyBytes = Base64.getEncoder().encodeToString(keyBytes);
//        System.out.println("Raw Key Bytes (Base64): " + encodedKeyBytes);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
