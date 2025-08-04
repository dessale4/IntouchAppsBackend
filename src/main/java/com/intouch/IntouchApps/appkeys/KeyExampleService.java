package com.intouch.IntouchApps.appkeys;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KeyExampleService {
    private final KeyExampleRepository keyExampleRepository;
    private final AppKeyRepository appKeyRepository;

    @Transactional
    public AppKey addKeyExample(KeyExample keyExample) {
        AppKey savedAppKey = appKeyRepository.findAppKeyByKeyFamilyIdAndKeyId(keyExample.getKeyFamilyId(), keyExample.getKeyId()).orElseThrow(() -> new RuntimeException("Some thing went wrong"));

        KeyExample existingDefaultKeyExample = savedAppKey.getDefaultKeyExample();
        if (existingDefaultKeyExample == null) {
            keyExample.setDefault(true);
            savedAppKey.setDefaultKeyExample(keyExample);
            savedAppKey.addKeyExample(keyExample);
            savedAppKey = appKeyRepository.save(savedAppKey);
        } else if (keyExample.isDefault()) {
            existingDefaultKeyExample.setDefault(false);
            keyExampleRepository.save(existingDefaultKeyExample);
            savedAppKey.setDefaultKeyExample(keyExample);
            savedAppKey.addKeyExample(keyExample);
            savedAppKey = appKeyRepository.save(savedAppKey);
        } else {
            savedAppKey.addKeyExample(keyExample);
            savedAppKey = appKeyRepository.save(savedAppKey);
        }
        return savedAppKey;
    }

    public AppKey setAsDefaultExample(Integer keyFamilyId, Integer keyId, Integer keyExampleId) {
        KeyExample savedKeyExample = keyExampleRepository.findById(keyExampleId).orElseThrow(() -> new RuntimeException("No key Example found with id: " + keyExampleId));
        AppKey savedAppKey = appKeyRepository.findAppKeyByKeyFamilyIdAndKeyId(keyFamilyId, keyId).orElseThrow(() -> new RuntimeException("AppKey Not found"));
        KeyExample savedDefaultKeyExample = savedAppKey.getDefaultKeyExample();
        savedDefaultKeyExample.setDefault(false);
        savedKeyExample.setDefault(true);
        keyExampleRepository.save(savedDefaultKeyExample);
        savedAppKey.setDefaultKeyExample(savedKeyExample);
        savedAppKey = appKeyRepository.save(savedAppKey);
        return savedAppKey;
    }
    public List<KeyExample> getKeyExamplesForAKey(Integer keyFamilyId, Integer keyId) {
        return keyExampleRepository.findByKeyFamilyIdAndKeyId(keyFamilyId, keyId);
    }
}
