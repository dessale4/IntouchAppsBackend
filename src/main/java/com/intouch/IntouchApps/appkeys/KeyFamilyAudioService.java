package com.intouch.IntouchApps.appkeys;

import com.intouch.IntouchApps.utils.AppObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class KeyFamilyAudioService {
    private final KeyFamilyAudioRepository keyFamilyAudioRepository;
    private final KeyFamilyRepository keyFamilyRepository;
    private final AppObjectMapper appObjectMapper;

    public KeyFamilyAudio setKeFamilyAudioTimeStamps(KeyFamilyAudioTimeStamp keyFamilyAudioTimeStamp, Integer keyFamilyId, Integer keyFamilyAudioId) {
        KeyFamilyAudio storedKeyFamilyAudio = keyFamilyAudioRepository.findByKeyFamilyIdAndId(keyFamilyId, keyFamilyAudioId).orElseThrow(() -> new RuntimeException("No KeyFamilyAudio found"));
        storedKeyFamilyAudio.setKeyOneTimeStamp(keyFamilyAudioTimeStamp.getKeyOneTimeStamp());
        storedKeyFamilyAudio.setKeyTwoTimeStamp(keyFamilyAudioTimeStamp.getKeyTwoTimeStamp());
        storedKeyFamilyAudio.setKeyThreeTimeStamp(keyFamilyAudioTimeStamp.getKeyThreeTimeStamp());
        storedKeyFamilyAudio.setKeyFourTimeStamp(keyFamilyAudioTimeStamp.getKeyFourTimeStamp());
        storedKeyFamilyAudio.setKeyFiveTimeStamp(keyFamilyAudioTimeStamp.getKeyFiveTimeStamp());
        storedKeyFamilyAudio.setKeySixTimeStamp(keyFamilyAudioTimeStamp.getKeySixTimeStamp());
        storedKeyFamilyAudio.setKeySevenTimeStamp(keyFamilyAudioTimeStamp.getKeySevenTimeStamp());
        storedKeyFamilyAudio = keyFamilyAudioRepository.save(storedKeyFamilyAudio);
        return storedKeyFamilyAudio;
    }

    public Set<KeyFamilyAudio> getKeyFamilyAudios(Integer keyFamilyId) {
        KeyFamily storedKeyFamily = keyFamilyRepository.findByKeyFamilyId(keyFamilyId).orElseThrow(() -> new RuntimeException("No keyFamily is found with keyFamilyId: " + keyFamilyId));
        return decryptKeyFamilyAudioUrls(storedKeyFamily.getKeyFamilyAudioSet());
    }

    private Set<KeyFamilyAudio> decryptKeyFamilyAudioUrls(Set<KeyFamilyAudio> keyFamilyAudios) {
        return keyFamilyAudios.stream()
                .map(audio -> appObjectMapper.decryptKeyFamilyAWSAudioLocation(audio))
                .collect(Collectors.toSet());
    }

    public KeyFamilyAudio setKeyFamilyAudioAsDefault(Integer keyFamilyId, Integer keyFamilyAudioId) {
        KeyFamilyAudio storedKeyFamilyAudio = keyFamilyAudioRepository.findByKeyFamilyIdAndId(keyFamilyId, keyFamilyAudioId).orElseThrow(() -> new RuntimeException("No KeyFamilyAudio found"));
        if (storedKeyFamilyAudio.isDefault()) {
            throw new RuntimeException("The KeyFamilyAudio is already set as default");
        }
        KeyFamily storedKeyFamily = keyFamilyRepository.findByKeyFamilyId(keyFamilyId).orElseThrow(() -> new RuntimeException("No keyFamily is found with keyFamilyId: " + keyFamilyId));

        KeyFamilyAudio defaultKeyFamilyAudio = storedKeyFamily.getKeyFamilyAudioSet().stream().filter(au->au.isDefault()).findFirst().get();
        defaultKeyFamilyAudio.setDefault(false);
//        keyFamilyAudioRepository.save(defaultKeyFamilyAudio);

        storedKeyFamilyAudio.setDefault(true);
        keyFamilyAudioRepository.saveAll(List.of(defaultKeyFamilyAudio, storedKeyFamilyAudio));
//        storedKeyFamily.setDefaultKeyFamilyAudio(storedKeyFamilyAudio);
//        keyFamilyRepository.save(storedKeyFamily);

        return storedKeyFamilyAudio;
    }
}
