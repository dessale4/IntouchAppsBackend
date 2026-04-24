package com.intouch.IntouchApps.security;

import com.intouch.IntouchApps.constants.ClientType;
import com.intouch.IntouchApps.enums.JwtTokenType;
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
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Arrays;

import io.jsonwebtoken.security.SignatureException;

import static com.intouch.IntouchApps.constants.ClientType.MOBILE_CLIENT;
import static com.intouch.IntouchApps.constants.ClientType.WEB_CLIENT;
import static com.intouch.IntouchApps.constants.CustomHeaders.CLIENT_TYPE;
import static com.intouch.IntouchApps.enums.JwtTokenType.ACCESS_TOKEN;
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
        String servletPath = request.getServletPath();
        final String clientTypeHeader = request.getHeader(CLIENT_TYPE);
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        if (clientTypeHeader == null ||
                !(MOBILE_CLIENT.equals(clientTypeHeader) ||
                        WEB_CLIENT.equals(clientTypeHeader))) {
            log.info("not an allowed client type => {} <====> {}", clientTypeHeader, servletPath);

            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE); // 503
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("""
                    {
                      "message": "Sorry, we are making updates to the service. Please check again later."
                    }
                    """);
//            log.info("not an allowed client type => " + clientTypeHeader +"<====>"+ servletPath);
//            exceptionResolver.resolveException(request, response, null, new RuntimeException("Sorry We are making updates to the service. Please check in some time."));
//            exceptionResolver.resolveException(request, response, null, new RuntimeException("Access not allowed now"));
            return;
        }

        if (servletPath.startsWith("/auth/")) {
            log.info("authentication is not required => " + servletPath);
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader(AUTHORIZATION);
        final String jwt;
        final String userEmail;
        //this happens when jwtRefreshToken expired and logout retried
        if (clientTypeHeader.equals(MOBILE_CLIENT) && servletPath.equals("/appUsers/logout") && authHeader == null) {
            return;
        }
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

                log.info("not a jwt Auth => " + servletPath);
                exceptionResolver.resolveException(request, response, null, new RuntimeException("Access not allowed"));
                return;
            }
            if (jwt == null || jwt.isEmpty() || jwt.isBlank()) {
                log.info("jwt not found => " + servletPath);
                exceptionResolver.resolveException(request, response, null, new RuntimeException("Access not allowed"));
                return;
            }

            userEmail = jwtService.extractUsername(jwt, ACCESS_TOKEN);
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                if (jwtService.isTokenValid(jwt, userDetails, ACCESS_TOKEN)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    log.info("not a valid jwt");
                    exceptionResolver.resolveException(request, response, null,
                            new RuntimeException("Not a valid jwt token"));
                    return;
                }
            } else if (userEmail == null) {
                log.info("userEmail not found => ");
                exceptionResolver.resolveException(request, response, null,
                        new RuntimeException("Access not allowed"));
                return;
            }
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException | SignatureException ex) {
            exceptionResolver.resolveException(request, response, null, ex);
        }
    }
//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest req) {
//        String p = req.getServletPath();
//        return p.equals("/actuator") || p.startsWith("/actuator/");
//    }

}
