package com.intouch.IntouchApps.security;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Arrays;

import io.jsonwebtoken.security.SignatureException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    @Autowired
    private JwtService jwtService;
    @Value("${server.servlet.context-path}")
    private String serverContextPath;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver exceptionResolver;//forwards the thrown exception to GlobalExceptionHandler

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        System.out.println(request.getHeader(AUTHORIZATION) + " current ServletPath =>" + request.getServletPath());
        if (request.getServletPath().contains("/auth/")) {
            log.info("authentication is not required => " + request.getServletPath());
            filterChain.doFilter(request, response);
            return;
        }
        final String authHeader = request.getHeader(AUTHORIZATION);
        final String jwt;
        final String userEmail;
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
//                System.out.println("authHeader ==> " + request.getServletPath());
                jwt = authHeader.substring(7);
            } else if (request.getCookies() != null) {
//                System.out.println("request.getCookies ==> " + request.getServletPath());
                jwt = Arrays.stream(request.getCookies())
                        .filter(c -> c.getName().equals("jwt"))
                        .findFirst()
                        .map(Cookie::getValue)
                        .orElse(null);
//                System.out.println(jwt + "is request jwt cookie for ==> " + request.getServletPath());
            } else {
                log.info("not a jwt Auth => " + request.getServletPath());
                exceptionResolver.resolveException(request, response, null, new RuntimeException("Access not allowed"));
                return;
            }
            if (jwt == null || jwt.isEmpty() || jwt.isBlank()) {
                log.info("jwt not found => " + request.getServletPath());
                exceptionResolver.resolveException(request, response, null, new RuntimeException("Access not allowed"));
                return;
            }
//            System.out.println("RequestURL : " + request.getRequestURL());
//            byte[] bytes = request.getHeader("Authorization").getBytes(StandardCharsets.UTF_8);
//            System.out.println(Arrays.toString(bytes));
//            System.out.println("JWT during request : " + jwt);
            userEmail = jwtService.extractUsername(jwt, false);
//            System.out.println("userEmail ===>" + userEmail);
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails, false)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    log.info("not a valid jwt");
                    exceptionResolver.resolveException(request, response, null, new RuntimeException("Not a valid jwt token"));
                    return;
                }
            }else if(userEmail == null){
                log.info("userEmail not found => ");
                exceptionResolver.resolveException(request, response, null, new RuntimeException("Access not allowed"));
                return;
            }
            filterChain.doFilter(request, response);
        } catch ( ExpiredJwtException | SignatureException | ParseException ex) {
            exceptionResolver.resolveException(request, response, null, ex);
        }
    }
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest req) {
//        String p = req.getServletPath();
//        return p.equals("/actuator") || p.startsWith("/actuator/");
//    }

}
