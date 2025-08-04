package com.intouch.IntouchApps.appkeys;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KeyFamilyAudioRepository extends JpaRepository<KeyFamilyAudio, Integer> {
    public Optional<KeyFamilyAudio> findByKeyFamilyIdAndId(Integer keyFamilyId, Integer keyId);
    public KeyFamilyAudio findByKeyFamilyIdAndKeyFamilyAudioFileName(Integer keyFamilyId, String fileName);
}
