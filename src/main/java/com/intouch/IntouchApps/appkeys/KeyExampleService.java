package com.intouch.IntouchApps.appkeys;

import com.intouch.IntouchApps.appkeys.dtos.KeyFamilyDefaultDTO;
import com.intouch.IntouchApps.appkeys.dtos.KeyFamilyDefaultMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class KeyExampleService {
    private final KeyExampleRepository keyExampleRepository;
    private final AppKeyRepository appKeyRepository;
    private final KeyFamilyDefaultMapper keyFamilyDefaultMapper;
    private final KeyFamilyRepository keyFamilyRepository;

    @Transactional
    public AppKey addKeyExample(KeyExample keyExample) {
        AppKey savedAppKey = appKeyRepository.findAppKeyByKeyFamilyIdAndKeyId(keyExample.getKeyFamilyId(), keyExample.getKeyId()).orElseThrow(() -> new RuntimeException("Some thing went wrong"));

//        KeyExample existingDefaultKeyExample = savedAppKey.getDefaultKeyExample();
        Optional<KeyExample> existingDefaultKeyExampleOptional = savedAppKey.getKeyExamples().stream().filter(eg -> eg.isDefault()).findFirst();
        if (!existingDefaultKeyExampleOptional.isPresent()) {
            keyExample.setDefault(true);
//            savedAppKey.setDefaultKeyExample(keyExample);
            savedAppKey.addKeyExample(keyExample);
            savedAppKey = appKeyRepository.save(savedAppKey);
        } else if (keyExample.isDefault()) {
            KeyExample existingDefaultKeyExample = existingDefaultKeyExampleOptional.get();
            existingDefaultKeyExample.setDefault(false);
            keyExampleRepository.save(existingDefaultKeyExample);
//            savedAppKey.setDefaultKeyExample(keyExample);
            savedAppKey.addKeyExample(keyExample);
            savedAppKey = appKeyRepository.save(savedAppKey);
        } else {
            savedAppKey.addKeyExample(keyExample);
            savedAppKey = appKeyRepository.save(savedAppKey);
        }
        return savedAppKey;
    }

    @Transactional
    public KeyFamilyDefaultDTO editKeyExample(Integer keyFamilyId, Integer keyExampleId, Map<String, Object> keyExample) {
        KeyExample existingKeyExample = keyExampleRepository.findById(keyExampleId).orElseThrow(() -> new EntityNotFoundException("The keyExample not found"));
        keyExample.forEach((k, v) -> {
            switch (k) {
                case "amharicName" -> existingKeyExample.setAmharicName((String) v);
                case "audioFileName" -> existingKeyExample.setAudioFileName((String) v);
                case "audioUrl" -> existingKeyExample.setAudioUrl((String) v);
                case "category" -> existingKeyExample.setCategory((String) v);
                case "englishName" -> existingKeyExample.setEnglishName((String) v);
                case "imageFileName" -> existingKeyExample.setImageFileName((String) v);
                case "imageUrl" -> existingKeyExample.setImageUrl((String) v);
                case "tigrinyaName" -> existingKeyExample.setTigrinyaName((String) v);
                default -> throw new IllegalArgumentException("Field not patchable: " + k);
            }
        });
        keyExampleRepository.save(existingKeyExample);
        KeyFamily updatedKeyFamily = keyFamilyRepository.findByKeyFamilyId(keyFamilyId).orElseThrow(() -> new EntityNotFoundException("KeyFamily not found"));
        return keyFamilyDefaultMapper.toKeyFamilyDefaultDTO(updatedKeyFamily);
    }

    public AppKey setAsDefaultExample(Integer keyFamilyId, Integer keyId, Integer keyExampleId) {
        KeyExample savedKeyExample = keyExampleRepository.findById(keyExampleId).orElseThrow(() -> new RuntimeException("No key Example found with id: " + keyExampleId));
        AppKey savedAppKey = appKeyRepository.findAppKeyByKeyFamilyIdAndKeyId(keyFamilyId, keyId).orElseThrow(() -> new RuntimeException("AppKey Not found"));
        KeyExample savedDefaultKeyExample = savedAppKey.getKeyExamples().stream().filter(eg -> eg.isDefault()).findFirst().get();
        savedDefaultKeyExample.setDefault(false);
        savedKeyExample.setDefault(true);
//        keyExampleRepository.save(savedDefaultKeyExample);
//        savedAppKey.setDefaultKeyExample(savedKeyExample);
        keyExampleRepository.saveAll(List.of(savedDefaultKeyExample, savedKeyExample));
        savedAppKey = appKeyRepository.save(savedAppKey);
        return savedAppKey;
    }

    public List<KeyExample> getKeyExamplesForAKey(Integer keyFamilyId, Integer keyId) {
        return keyExampleRepository.findByKeyFamilyIdAndKeyId(keyFamilyId, keyId);
    }
}
