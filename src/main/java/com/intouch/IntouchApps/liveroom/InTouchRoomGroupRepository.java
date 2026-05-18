package com.intouch.IntouchApps.liveroom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InTouchRoomGroupRepository
        extends JpaRepository<InTouchRoomGroup, Long> {

    List<InTouchRoomGroup> findByRoomIdOrderBySortOrderAsc(Long roomId);

    boolean existsByRoomIdAndNameIgnoreCase(Long roomId, String name);

    @Modifying
    @Query("""
                UPDATE InTouchRoomGroup g
                SET g.completedAt = null,
                    g.errorCount = 0,
                    g.score = 0,
                    g.completedPatternCount = 0
                WHERE g.room.id = :roomId
            """)
    void resetProgressByRoomId(@Param("roomId") Long roomId);

    long countByRoomId(Long roomId);

    void deleteByRoomId(Long roomId);
}