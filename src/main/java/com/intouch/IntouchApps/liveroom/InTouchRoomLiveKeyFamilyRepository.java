package com.intouch.IntouchApps.liveroom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InTouchRoomLiveKeyFamilyRepository
        extends JpaRepository<InTouchRoomLiveKeyFamily, Long> {

    List<InTouchRoomLiveKeyFamily> findByRoomIdAndActiveTrueOrderByRowIndexAsc(Long roomId);

    @Modifying
    @Query("""
                DELETE FROM InTouchRoomLiveKeyFamily f
                WHERE f.room.id = :roomId
            """)
    void deleteByRoomId(@Param("roomId") Long roomId);
}
