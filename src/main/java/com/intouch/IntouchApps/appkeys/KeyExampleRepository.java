package com.intouch.IntouchApps.appkeys;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KeyExampleRepository extends JpaRepository<KeyExample, Integer> {
    public KeyExample findByImageFileName(String imageFileNAme);
    public KeyExample findByAudioFileName(String audioFileNAme);
    public List<KeyExample> findByKeyFamilyIdAndKeyId(Integer keyFamilyId, Integer keyId);
}
