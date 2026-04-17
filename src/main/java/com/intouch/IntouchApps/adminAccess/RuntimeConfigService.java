package com.intouch.IntouchApps.adminAccess;

import com.intouch.IntouchApps.auth.AuthenticationService;
import com.intouch.IntouchApps.handler.AccountNotActivatedException;
import com.intouch.IntouchApps.user.VerificationToken;
import com.intouch.IntouchApps.user.TokenRepository;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@RefreshScope
@Transactional
public class RuntimeConfigService {
    private final AuthenticationService service;
    private final RestTemplate restTemplate;
    private final AppConfigRepository configRepository;
    private final ConfigurableEnvironment environment;
    @Autowired
    @Qualifier("runtimeEncryptor")
    private StringEncryptor encryptor;
    private final StandardPBEStringEncryptor standardPBEStringEncryptor;
    private static final String RUNTIME_PROPERTY_SOURCE_NAME = "runtimeOverrides";
    // In-memory cache for quick lookup
    private final Map<String, String> cache = new HashMap<>();

    @Value("${application.security.jwt.secret_key}")
    private String currentJwtAccessSecret;

    @Value("${application.security.jwt.refresh_token.secret_key}")
    private String currentJwtRefreshSecret;

    @Value("${application.encryption.appConfigPassword}")
    private String currentJasyptPassword;
    private final TokenRepository tokenRepository;

    /**
     * Load all configs from DB and override environment properties
     */
    public void reloadConfigs(String configCode, Principal principal, HttpServletRequest httpServletRequest) {
        if (!validateAppConfigCode(configCode, principal)) throw new RuntimeException("Not a valid code");
        List<AppConfig> configs = configRepository.findAll();
        Map<String, Object> map = new HashMap<>();
        for (AppConfig cfg : configs) {
            String value = cfg.getPropertyType().equals("SECRET") ? encryptor.decrypt(cfg.getPropertyValue()) : cfg.getPropertyValue();

            map.put(cfg.getPropertyKey(), value);
//            environment.getSystemProperties().put(cfg.getPropertyKey(), value);
            cache.put(cfg.getPropertyKey(), value);
            // Handle JWT rotation
            if ("APPLICATION_SECURITY_JWT_SECRET_KEY".equals(cfg.getPropertyKey())) currentJwtAccessSecret = value;
            if ("APPLICATION_SECURITY_JWT_REFRESH_SECRET_KEY".equals(cfg.getPropertyKey()))
                currentJwtRefreshSecret = value;
            // Handle Jasypt password rotation
            if ("APP_CONFIG_ENCRYPTION_PASSWORD".equals(cfg.getPropertyKey())) currentJasyptPassword = value;
        }
//        System.out.println(map);
        PropertySource<?> ps = new MapPropertySource(RUNTIME_PROPERTY_SOURCE_NAME, map);
        // Add it with higher precedence than application.properties
        environment.getPropertySources().addFirst(ps);
    }
//    triggerActuatorRefresh method helps to apply new env props to the existing beans
//    Works in presence of Cloud Config Server setup
    private void triggerActuatorRefresh(HttpServletRequest httpServletRequest) {
         String baseUrl = ServletUriComponentsBuilder.fromRequestUri(httpServletRequest)
                .replacePath(null) // Remove the path (i.e on commenting out this line we get the full request url)
                .build()
                .toUriString();
        String refreshUrl = baseUrl + "/actuator/refresh"; // Adjust port and context path if necessary
//        System.out.println("refreshUrl => " + refreshUrl);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(refreshUrl, null, String.class);
            // You can log the response or handle it as needed
//            System.out.println("Refresh endpoint triggered. Response: " + response.getStatusCode());
        } catch (Exception e) {
            System.err.println("Error triggering refresh endpoint: " + e.getMessage());
        }
    }

    /**
     * Get a property from cache or environment
     */
    public String getProperty(String key, String configCode, Principal principal) {
        if (!validateAppConfigCode(configCode, principal)) throw new RuntimeException("Not a valid code");
//        return cache.getOrDefault(key, environment.getProperty(key));
        return environment.getProperty(key);
    }
    public Object getProperty(String envProperty) {
        return environment.getProperty(envProperty);
    }

    /**
     * Encrypt value using current encryptor
     */
    public String encryptValue(String plain, String configCode, Principal principal) {
        if (!validateAppConfigCode(configCode, principal)) throw new RuntimeException("Not a valid code");
        return encryptor.encrypt(plain);
    }

    /**
     * Manual add/update config
     */
    public AppConfig saveOrUpdate(String key, String value, String type, String version, String configCode, Principal principal) {
        if (!validateAppConfigCode(configCode, principal)) throw new RuntimeException("Not a valid code");
        for (String ignore : List.of(
                "APPLICATION_SECURITY_JWT_SECRET_KEY", "application.security.jwt.secret_key",
                "APPLICATION_SECURITY_JWT_REFRESH_SECRET_KEY", "application.security.jwt.refresh_token.secret_key",
                "APP_CONFIG_ENCRYPTION_PASSWORD", "application.encryption.appConfigPassword",
                "JASYPT_ENCRYPTION_PASSWORD", "application.encryption.jasyptEncryptionPassword", "application.payment.enabled")) {
            if (key.equalsIgnoreCase(ignore)) {
                throw new RuntimeException("Not an allowed operation for the provided key");
            }
        }

        AppConfig cfg = configRepository.findByPropertyKey(key)
                .orElse(AppConfig.builder().propertyKey(key).build());

        cfg.setPropertyValue(type.equals("SECRET") ? encryptValue(value, configCode, principal) : value);
        cfg.setPropertyType(type);
        cfg.setKeyVersion(version);
        configRepository.save(cfg);
//        environment.getSystemProperties().put(key,value);
//        reloadConfigs(); // apply immediately
        return cfg;
    }

    public String getConfigCode(String configKey, Principal principal) throws MessagingException, AccountNotActivatedException {
        service.confirmEmailRequest(standardPBEStringEncryptor.decrypt(principal.getName()), configKey);
        return "Config code sent to " + standardPBEStringEncryptor.decrypt(principal.getName());
    }

    private boolean validateAppConfigCode(String configCode, Principal principal) {
        return service.validateAppConfigCode(configCode, principal);
    }

    @Transactional
    public String deleteConfigCode(String configCode, Principal principal) {
        VerificationToken configVerificationToken = tokenRepository.findByToken(configCode).orElseThrow(() -> new IllegalArgumentException("Config code not found"));
        if (!configVerificationToken.getUser().getEmail().equals(principal.getName())) {
            throw new RuntimeException("You are not authorized to do so.");
        }
        tokenRepository.delete(configVerificationToken);
        return "Config code deleted successfully!";
    }


}

