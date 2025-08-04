package com.intouch.IntouchApps.appkeys;

import com.intouch.IntouchApps.utils.AppObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KeyFamilyService {
    private final KeyFamilyRepository keyFamilyRepository;
    private final AppObjectMapper appObjectMapper;

    @Transactional
    public KeyFamily addKeyBasics(AppKeyRequest AppKeyRequest) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        KeyFamily keyFamily = keyFamilyRepository.findByKeyFamilyId(AppKeyRequest.getKeyFamilyId()).orElseThrow(() -> new RuntimeException("Some thing went wrong"));
        AppKey appKey = AppKey.builder()
                .keyId(AppKeyRequest.getKeyId())
                .keyName(AppKeyRequest.getKeyName())
                .keyNameInEnglish(AppKeyRequest.getKeyNameInEnglish())
                .keyFamilyId(AppKeyRequest.getKeyFamilyId()).build();

        KeyFamily updatedKeyFamily = linkAppKeyToKeyFamily(keyFamily, appKey, AppKeyRequest.getKeyId());

        KeyFamily persistedKeyFamily = keyFamilyRepository.save(updatedKeyFamily);
        return persistedKeyFamily;
    }

    private KeyFamily linkAppKeyToKeyFamily(KeyFamily keyFamily, AppKey appKey, Integer keyId) {
        switch (keyId) {
            case 1: {
                keyFamily.setKeyOne(appKey);
                break;
            }
            case 2: {
                keyFamily.setKeyTwo(appKey);
                break;
            }
            case 3: {
                keyFamily.setKeyThree(appKey);
                break;
            }
            case 4: {
                keyFamily.setKeyFour(appKey);
                break;
            }
            case 5: {
                keyFamily.setKeyFive(appKey);
                break;
            }
            case 6: {
                keyFamily.setKeySix(appKey);
                break;
            }
            case 7: {
                keyFamily.setKeySeven(appKey);
                break;
            }
        }
        return keyFamily;
    }

    public List<KeyFamily> getAllKeyFamilies() {
        Sort sort = Sort.by(Sort.Direction.ASC, "keyFamilyId");
        List<KeyFamily> keyFamilies = keyFamilyRepository.findAll(sort);
        List<KeyFamily> decryptedKeyFamilies = keyFamilies.stream()
                .map(kf -> appObjectMapper.decryptKeyFamily(kf))
                .collect(Collectors.toList());
        return decryptedKeyFamilies;
    }

    public List<KeyFamilyResponse> getKeyFamiliesWithDefaultExamples() {
        Sort sort = Sort.by(Sort.Direction.ASC, "keyFamilyId");
        List<KeyFamily> keyFamilyList = keyFamilyRepository.findAll(sort);

        return keyFamilyList.stream()
                .map((kf) -> appObjectMapper.mapKeyFamilyToKeyFamilyResponse(kf))
                .collect(Collectors.toList());
    }

    public KeyFamilyResponse getKeyFamilyWithDefaultExamples(Integer keyFamilyId) {
        KeyFamily storedkeyFamily = keyFamilyRepository.findByKeyFamilyId(keyFamilyId).orElseThrow(() -> new RuntimeException("No key family found with keyFamilyId: " + keyFamilyId));
        return appObjectMapper.mapKeyFamilyToKeyFamilyResponse(storedkeyFamily);
    }
}
