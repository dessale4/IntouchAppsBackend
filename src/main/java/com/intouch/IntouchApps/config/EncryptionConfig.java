package com.intouch.IntouchApps.config;

import lombok.RequiredArgsConstructor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.jasypt.salt.ZeroSaltGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@RefreshScope
public class EncryptionConfig {
    @Value("${application.encryption.jasyptEncryptionPassword}")
    private String password;
    @Value("${application.encryption.jasyptEncryptionAlgorithm}")
    private String algorithm;
    @Value("${application.encryption.jasyptEncryptionSaltGeneratorClassName}")
    private String className;
    @Value("${application.encryption.jasyptIvGeneratorClassName}")
    private String ivGeneratorClassName;
    @Bean
    public StandardPBEStringEncryptor standardPBEStringEncryptor(){
//
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(password); // encryptor's private key
        config.setAlgorithm(algorithm);
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv."+ivGeneratorClassName);
        config.setStringOutputType("base64");
        encryptor.setConfig(config);
        return  encryptor;
    }
}
