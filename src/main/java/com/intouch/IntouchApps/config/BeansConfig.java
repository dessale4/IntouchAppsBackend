package com.intouch.IntouchApps.config;

import com.intouch.IntouchApps.constants.CustomHeaders;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

import static com.intouch.IntouchApps.constants.CustomHeaders.CLIENT_TYPE;
import static com.intouch.IntouchApps.constants.CustomHeaders.CONFIG_CODE;
import static org.springframework.http.HttpHeaders.*;

@Configuration
@RequiredArgsConstructor
//@RefreshScope
public class BeansConfig {
    @Value("${application.corsAllowedOrigins}")
    private List<String> allowedOriginsList;
    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);// Needed for withCredentials: true
        //todo prepare desired list for allowed origins
        config.setAllowedOrigins(allowedOriginsList);
        config.setAllowedHeaders(Arrays.asList(
                ORIGIN,
                CONTENT_TYPE,
                ACCEPT,
                AUTHORIZATION,
                CONFIG_CODE,//using alt + enter shortcut
                CLIENT_TYPE
        ));
        config.setAllowedMethods(Arrays.asList(
                "GET",
                "POST",
                "DELETE",
                "PUT",
                "PATCH",
                "OPTIONS"
        ));
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
