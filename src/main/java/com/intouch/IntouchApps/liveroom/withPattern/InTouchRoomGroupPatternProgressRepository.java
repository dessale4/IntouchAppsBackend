package com.intouch.IntouchApps.liveroom.withPattern;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InTouchRoomGroupPatternProgressRepository
        extends JpaRepository<InTouchRoomGroupPatternProgress, Long> {

    Optional<InTouchRoomGroupPatternProgress> findByGroupIdAndPatternId(
            Long groupId,
            Long patternId
    );

    long countByRoomIdAndGroupIdAndCompletedTrue(Long roomId, Long groupId);
}