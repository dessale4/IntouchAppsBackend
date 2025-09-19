package com.intouch.IntouchApps.appkeys.dtos;

import com.intouch.IntouchApps.appkeys.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;

@Mapper(componentModel = "spring", uses = EncryptionMapper.class)
public abstract class KeyFamilyDefaultMapper {

    // ----------------- DEFAULT VIEW -----------------
    @Mapping(source = "keyFamilyAudioSet", target = "defaultKeyFamilyAudio", qualifiedByName = "mapDefaultKeyFamilyAudio")
    @Mapping(source = "keyOne", target = "keyOne")
    @Mapping(source = "keyTwo", target = "keyTwo")
    @Mapping(source = "keyThree", target = "keyThree")
    @Mapping(source = "keyFour", target = "keyFour")
    @Mapping(source = "keyFive", target = "keyFive")
    @Mapping(source = "keySix", target = "keySix")
    @Mapping(source = "keySeven", target = "keySeven")
    public abstract KeyFamilyDefaultDTO toKeyFamilyDefaultDTO(KeyFamily order);
    @Named("mapDefaultKeyFamilyAudio")
    KeyFamilyAudioDTO mapDefaultKeyFamilyAudio(Set<KeyFamilyAudio> keyFamilyAudios) {
        if (keyFamilyAudios == null || keyFamilyAudios.isEmpty()) return null;
        return keyFamilyAudios.stream()
                .filter(KeyFamilyAudio::isDefault)
                .findFirst()
                .map(this::toKeyFamilyAudioDTO)
                .orElse(null);
    }

//    @Mapping(source = "keyFamilyAudioUrl", target = "keyFamilyAudioUrl", qualifiedByName = "decrypt")
    abstract KeyFamilyAudioDTO toKeyFamilyAudioDTO(KeyFamilyAudio keyFamilyAudio);

    @Mapping(source = "keyExamples", target = "defaultKeyExample", qualifiedByName = "mapDefaultKeyExample")
    @Mapping(source = "keyAudios", target = "defaultKeyAudio", qualifiedByName = "mapDefaultKeyAudio")
    public abstract AppKeyDefaultDTO toAppKeyDefaultDTO(AppKey appKey);

    @Named("mapDefaultKeyExample")
    public KeyExampleDTO mapDefaultKeyExample(Set<KeyExample> keyExamples) {
        if (keyExamples == null || keyExamples.isEmpty()) return null;
        return keyExamples.stream()
                .filter(KeyExample::isDefault)
                .findFirst()
                .map(this::toKeyExampleDTO)
                .orElse(null);
    }

//    @Mapping(source = "imageUrl", target = "imageUrl", qualifiedByName = "decrypt")
//    @Mapping(source = "audioUrl", target = "audioUrl", qualifiedByName = "decrypt")
    public abstract KeyExampleDTO toKeyExampleDTO(KeyExample keyExample);

    @Named("mapDefaultKeyAudio")
    public KeyAudioDTO mapDefaultKeyAudio(Set<KeyAudio> keyAudios) {
        if (keyAudios == null || keyAudios.isEmpty()) return null;
        return keyAudios.stream()
                .filter(KeyAudio::isDefault)
                .findFirst()
                .map(this::toKeyAudioDTO)
                .orElse(null);
    }

//    @Mapping(source = "keyAudioUrl", target = "keyAudioUrl", qualifiedByName = "decrypt")
    public abstract KeyAudioDTO toKeyAudioDTO(KeyAudio keyAudio);
}
