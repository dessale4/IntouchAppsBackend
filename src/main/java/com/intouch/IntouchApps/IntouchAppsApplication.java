package com.intouch.IntouchApps;

import com.intouch.IntouchApps.appkeys.KeyFamily;
import com.intouch.IntouchApps.appkeys.KeyFamilyRepository;
import com.intouch.IntouchApps.role.Role;
import com.intouch.IntouchApps.role.RoleRepository;
//import com.intouch.IntouchApps.security.StringEncryptConverter;
import com.intouch.IntouchApps.user.User;
import com.intouch.IntouchApps.user.UserRepository;
import com.intouch.IntouchApps.utils.AppDateUtil;
import com.intouch.IntouchApps.utils.UserAndRolesUtil;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import jakarta.annotation.PostConstruct;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootApplication
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
    @PostConstruct
    public void initSetUp(){
        //keep default timezone setup here
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
    @Bean
    public CommandLineRunner runner(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder, KeyFamilyRepository keyFamilyRepository, StandardPBEStringEncryptor standardPBEStringEncryptor) throws ParseException {

        return args -> {
            List<Role> roles = null;
            if (roleRepository.findAll().size() == 0) {

                roles = roleRepository.saveAll(UserAndRolesUtil.initRoles);
            }
            if (userRepository.findAll().size() == 0) {

                List<User> initUsers = UserAndRolesUtil.initUsers.stream()
                        .map(u->{
                            u.setPassword(passwordEncoder.encode(u.getPassword()));
                            u.setEmail(standardPBEStringEncryptor.encrypt(u.getEmail().toLowerCase()));
                            return u;
                        })
                                .collect(Collectors.toList());
                userRepository.saveAll(initUsers);
                //placeholder for keyFamilies
                List<KeyFamily> keyFamilies = Arrays.stream(IntStream.rangeClosed(0, 41).toArray())
                        .mapToObj(index -> KeyFamily.builder().keyFamilyId(index)
                                .build())
                        .collect(Collectors.toList());
                keyFamilyRepository.saveAll(keyFamilies);
            }
        };
    }
}
