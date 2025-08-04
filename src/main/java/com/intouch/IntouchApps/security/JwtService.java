package com.intouch.IntouchApps.security;

import com.intouch.IntouchApps.utils.AppDateUtil;
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
    public String extractUsername(String token) {

        return extractClaim(token, Claims::getSubject);
    }
    public <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    public String generateToken(UserDetails userDetails) throws ParseException {
        return generateToken(new HashMap<>(), userDetails);
    }
    public String generateToken(Map<String,Object> claims, UserDetails userDetails) throws ParseException {
        return buildToken(claims, userDetails
//                , jwtExpiration
        );
    }
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
//            ,
//            long jwtExpiration
    ) throws ParseException {
        var authorities = userDetails
                            .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                //recheck Date obj and CurrentUTCDateTime scenario (occurred in 3 places)
                .issuedAt(new Date(System.currentTimeMillis()))
//                .issuedAt(new Date(AppDateUtil.getCurrentUTCTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
//                .setExpiration(new Date(AppDateUtil.getCurrentUTCTimeMillis() + jwtExpiration))
                .claim("authorities", authorities) //add claims
                .signWith(getSignInKey())
                .compact();
    }
    public boolean isTokenValid(String token, UserDetails userDetails) throws ParseException {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    private boolean isTokenExpired(String token) throws ParseException {
        Date expirationDate = extractExpiration(token);
        System.out.println("expirationDate ==>" + expirationDate);
       Date dateNow =  new Date();
        System.out.println("dateNow ==>" + dateNow);
       return expirationDate.before(dateNow);
//        return extractExpiration(token).before(new Date());
//        return extractExpiration(token).before(AppDateUtil.getDateFromCurrentUTCDateTime());
    }
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
