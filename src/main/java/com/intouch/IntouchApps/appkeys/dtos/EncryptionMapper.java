package com.intouch.IntouchApps.appkeys.dtos;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
public class EncryptionMapper {
    private final StandardPBEStringEncryptor encryptor;
    public EncryptionMapper(StandardPBEStringEncryptor encryptor) {
        this.encryptor = encryptor;
    }
    @Named("decrypt")
    public String decrypt(String encrypted) {
        if (encrypted == null) return null;
        return encryptor.decrypt(encrypted);
    }
}
