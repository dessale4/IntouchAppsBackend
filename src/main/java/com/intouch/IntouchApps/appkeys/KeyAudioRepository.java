package com.intouch.IntouchApps.appkeys;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KeyAudioRepository extends JpaRepository<KeyAudio, Integer> {
}
