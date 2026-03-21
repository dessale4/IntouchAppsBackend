package com.intouch.IntouchApps.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {
    private final AuthenticationProvider authenticationProvider;
    private final JwtFilter jwtAuthFilter;
    private final PasswordEncoder passwordEncoder;

    @Bean(name = "actuatorUsers")
    InMemoryUserDetailsManager actuatorUsers(
            @Value("${ACT_USER:actuator}") String user,
            @Value("${ACT_PASS:change-me}") String pass,
            PasswordEncoder enc) {

        return new InMemoryUserDetailsManager(
                User.withUsername(user)
                        .password(enc.encode(pass))    // for quick local test you can use .password("{noop}change-me")
                        .authorities("ACTUATOR")       // matches .hasAuthority("ACTUATOR")
                        .build()
        );
    }

    //    @Bean @Order(1)
//    SecurityFilterChain actuator(HttpSecurity http,
//                                 @Qualifier("actuatorUsers") UserDetailsService uds,
//                                 PasswordEncoder enc) throws Exception {
//
//        var provider = new DaoAuthenticationProvider();
//        provider.setUserDetailsService(uds);
//        provider.setPasswordEncoder(enc);
//
//        http
//                .securityMatcher("/actuator/**")
////                .authenticationProvider(provider)                    // << tie this chain to that user store
//                .authorizeHttpRequests(a -> a
//                        .requestMatchers("/actuator/**").permitAll()
//                        .anyRequest().permitAll())
////                        .requestMatchers("/actuator/health").permitAll()
////                        .anyRequest().hasAuthority("ACTUATOR"))
//                .httpBasic(Customizer.withDefaults())
//                .csrf(csrf -> csrf.disable())
//                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
//        return http.build();
//    }
    @Bean
    @Order(1)
    SecurityFilterChain actuator(HttpSecurity http,
                                 @Qualifier("actuatorUsers") UserDetailsService uds,
                                 PasswordEncoder enc) throws Exception {

        var provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(uds);
        provider.setPasswordEncoder(enc);

        http
                .securityMatcher("/actuator/**")
                .authenticationProvider(provider)
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/actuator/prometheus").hasAuthority("ACTUATOR")
                        .anyRequest().hasAuthority("ACTUATOR")
                )
                .httpBasic(withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
//                .requiresChannel(channel -> channel.anyRequest().requiresSecure())// Enforce HTTPS for all requests
                .cors(withDefaults())//enforces CorsFilter bean config
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req ->
                        req.requestMatchers(
                                        "auth/**",
                                        "/v2/api-docs",
                                        "/v3/api-docs",
                                        "/v3/api-docs/**",
                                        "/swagger-resources",
                                        "/swagger-resources/**",
                                        "/configuration/ui",
                                        "/configuration/security",
                                        "/swagger-ui/**",
                                        "/webjars/**",
                                        "/swagger-ui.html"
                                )
                                .permitAll()
                                .anyRequest()
                                .authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
