package com.intouch.IntouchApps;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.TimeZone;

@RefreshScope// helps to update envs if they got updated at runtime
@SpringBootApplication
@EnableCaching  //sets up a cache manager and creates in-memory cache using concurrent hashmap
@EnableJpaAuditing //helps in auto date entry and auto date modification of entities
@EnableAsync
@EnableEncryptableProperties //to enable jasypt encryption
//@PropertySource(name="EncryptedProperties", value = "classpath:application.yml")
//@EnableConfigurationProperties
//@VaultPropertySources({})
//@VaultPropertySource("InTouchApps/appSecrets")//for custom secrete engine
public class IntouchAppsApplication {
//    @Value("${DB_URL}")//from custom vault secrete engine
//    private String vaultTest;
//    @Value("${DB_URL_DEFAULT}")//from default vault secrete engine
//    private String vaultTest1;
//    @Value("${application.security.jwt.expiration}")
//    private long jwtExpiration;

    public static void main(String[] args) {
        SpringApplication.run(IntouchAppsApplication.class, args);
    }

}
