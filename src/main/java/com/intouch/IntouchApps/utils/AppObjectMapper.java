package com.intouch.IntouchApps.utils;

import com.intouch.IntouchApps.appkeys.*;
import com.intouch.IntouchApps.enums.AppConstantsEnum;
import lombok.RequiredArgsConstructor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AppObjectMapper {
    private String tigrinya = AppConstantsEnum.TIGRINYA.name();
    private final StandardPBEStringEncryptor standardPBEStringEncryptor;
//    public KeyFamilyResponse mapKeyFamilyToKeyFamilyResponse(KeyFamily keyFamily){
//        return KeyFamilyResponse.builder()
//                .keyFamilyId(keyFamily.getKeyFamilyId())
//                .defaultKeyFamilyAudio(keyFamily.getDefaultKeyFamilyAudio() != null ? decryptKeyFamilyAWSAudioLocation(keyFamily.getDefaultKeyFamilyAudio()): keyFamily.getDefaultKeyFamilyAudio())
//                .keyOne(mapAppKeyToAppKeyResponse(keyFamily.getKeyOne()))
//                .keyTwo(mapAppKeyToAppKeyResponse(keyFamily.getKeyTwo()))
//                .keyThree(mapAppKeyToAppKeyResponse(keyFamily.getKeyThree()))
//                .keyFour(mapAppKeyToAppKeyResponse(keyFamily.getKeyFour()))
//                .keyFive(mapAppKeyToAppKeyResponse(keyFamily.getKeyFive()))
//                .keySix(mapAppKeyToAppKeyResponse(keyFamily.getKeySix()))
//                .keySeven(mapAppKeyToAppKeyResponse(keyFamily.getKeySeven()))
//                .build();
//    }

    public KeyFamilyAudio decryptKeyFamilyAWSAudioLocation(KeyFamilyAudio keyFamilyAudio){
//        KeyFamilyAudio copiedKeyFamilyAudio = new KeyFamilyAudio();
//        BeanUtils.copyProperties(keyFamilyAudio, copiedKeyFamilyAudio);
        return KeyFamilyAudio.builder()
                .keyFamilyAudioUrl(keyFamilyAudio.getKeyFamilyAudioUrl() ==null ?keyFamilyAudio.getKeyFamilyAudioUrl() : standardPBEStringEncryptor.decrypt(keyFamilyAudio.getKeyFamilyAudioUrl()))
                .keyFamilyId(keyFamilyAudio.getKeyFamilyId())
                .keyFamilyAudioFileName(keyFamilyAudio.getKeyFamilyAudioFileName())
                .isDefault(keyFamilyAudio.isDefault())
                .keyOneTimeStamp(keyFamilyAudio.getKeyOneTimeStamp())
                .keyTwoTimeStamp(keyFamilyAudio.getKeyTwoTimeStamp())
                .keyThreeTimeStamp(keyFamilyAudio.getKeyThreeTimeStamp())
                .keyFourTimeStamp(keyFamilyAudio.getKeyFourTimeStamp())
                .keyFiveTimeStamp(keyFamilyAudio.getKeyFiveTimeStamp())
                .keySixTimeStamp(keyFamilyAudio.getKeySixTimeStamp())
                .keySevenTimeStamp(keyFamilyAudio.getKeySevenTimeStamp())
                .build();
//        if(keyFamilyAudio.getKeyFamilyAudioUrl() !=null){
//            String decryptedKeyFamilyAudioUrl = standardPBEStringEncryptor.decrypt(keyFamilyAudio.getKeyFamilyAudioUrl());
//            copiedKeyFamilyAudio.setKeyFamilyAudioUrl(decryptedKeyFamilyAudioUrl);
//        }
//       return copiedKeyFamilyAudio;
    }

    public KeyExample decryptKeyExampleAWSImageAndAudioLocation(KeyExample keyExample){
        keyExample.setImageUrl(keyExample.getImageUrl() != null ? standardPBEStringEncryptor.decrypt(keyExample.getImageUrl()) : keyExample.getImageUrl());
        keyExample.setAudioUrl(keyExample.getAudioUrl() != null ? standardPBEStringEncryptor.decrypt(keyExample.getAudioUrl()) : keyExample.getAudioUrl());
        return keyExample;
    }
//    public AppKeyResponse mapAppKeyToAppKeyResponse(AppKey appKey){
//        if(appKey !=null) {
//            return AppKeyResponse.builder()
//                    .keyId(appKey.getKeyId())
//                    .keyFamilyId(appKey.getKeyFamilyId())
//                    .keyName(appKey.getKeyName())
//                    .keyInEnglish(appKey.getKeyNameInEnglish())
//                    .defaultKeyAudio(appKey.getDefaultKeyAudio() != null ? decryptKeyAudioAWSUrl(appKey.getDefaultKeyAudio()) : appKey.getDefaultKeyAudio())
//                    .defaultKeyExample(appKey.getDefaultKeyExample() != null ? decryptKeyExampleImageAndAudioAWSUrls(appKey.getDefaultKeyExample()) : appKey.getDefaultKeyExample())
//                    .build();
//        }else{
//            return AppKeyResponse.builder().build();
//        }
//    }

    private KeyAudio decryptKeyAudioAWSUrl(KeyAudio defaultKeyAudio) {
//        defaultKeyAudio.setKeyAudioUrl(defaultKeyAudio.getKeyAudioUrl() != null ? standardPBEStringEncryptor.decrypt(defaultKeyAudio.getKeyAudioUrl()) : defaultKeyAudio.getKeyAudioUrl());
        defaultKeyAudio.setKeyAudioUrl(defaultKeyAudio.getKeyAudioUrl());
        return defaultKeyAudio;
    }

    public KeyExample decryptKeyExampleImageAndAudioAWSUrls(KeyExample keyExample){
        return decryptKeyExampleAWSImageAndAudioLocation(keyExample);
    }

    public KeyFamily decryptKeyFamily(KeyFamily keyFamily){
        keyFamily.setKeyOne(decryptAppKey(keyFamily.getKeyOne()));
        keyFamily.setKeyTwo(decryptAppKey(keyFamily.getKeyTwo()));
        keyFamily.setKeyThree(decryptAppKey(keyFamily.getKeyThree()));
        keyFamily.setKeyFour(decryptAppKey(keyFamily.getKeyFour()));
        keyFamily.setKeyFive(decryptAppKey(keyFamily.getKeyFive()));
        keyFamily.setKeySix(decryptAppKey(keyFamily.getKeySix()));
        keyFamily.setKeySeven(decryptAppKey(keyFamily.getKeySeven()));
        return keyFamily;
    }
    public AppKey decryptAppKey(AppKey appKey){
        if(appKey != null){
            Set<KeyExample> keyExamples = appKey.getKeyExamples().stream()
                    .map(ex -> ex != null ? decryptKeyExampleImageAndAudioAWSUrls(ex) : ex)
                    .collect(Collectors.toSet());
            appKey.setKeyExamples(keyExamples);
            Set<KeyAudio> keyAudios = appKey.getKeyAudios().stream()
                    .map(au -> au != null ? decryptKeyAudioAWSUrl(au) : au)
                    .collect(Collectors.toSet());
            appKey.setKeyAudios(keyAudios);
        }
        return appKey;
    }
}
