package com.intouch.IntouchApps.liveroom.withPattern;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InTouchRoomGroupPatternProgressRepository
        extends JpaRepository<InTouchRoomGroupPatternProgress, Long> {

    Optional<InTouchRoomGroupPatternProgress> findByGroupIdAndPatternId(
            Long groupId,
            Long patternId
    );

    @Modifying
    @Query("""
    DELETE FROM InTouchRoomGroupPatternProgress p
    WHERE p.room.id = :roomId
""")
    void deleteByRoomId(@Param("roomId") Long roomId);
    long countByRoomIdAndGroupIdAndCompletedTrue(Long roomId, Long groupId);
}