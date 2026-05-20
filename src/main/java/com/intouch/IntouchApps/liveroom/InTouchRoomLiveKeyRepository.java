package com.intouch.IntouchApps.liveroom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InTouchRoomLiveKeyRepository
        extends JpaRepository<InTouchRoomLiveKey, Long> {

    List<InTouchRoomLiveKey> findByRoomIdAndActiveTrueOrderBySortOrderAsc(Long roomId);

    long countByRoomIdAndActiveTrue(Long roomId);
    @Modifying
    @Query("""
                DELETE FROM InTouchRoomLiveKey k
                WHERE k.room.id = :roomId
            """)
    void deleteByRoomId(@Param("roomId") Long roomId);
}
