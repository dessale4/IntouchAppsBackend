package com.intouch.IntouchApps.liveroom.repository;

import com.intouch.IntouchApps.liveroom.entity.InTouchRoomGroupBoardRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InTouchRoomGroupBoardRowRepository
        extends JpaRepository<InTouchRoomGroupBoardRow, Long> {

    Optional<InTouchRoomGroupBoardRow> findByRoomIdAndGroupIdAndRowIndex(
            Long roomId,
            Long groupId,
            Integer rowIndex
    );

    @Modifying
    @Query("""
                DELETE FROM InTouchRoomGroupBoardRow r
                WHERE r.room.id = :roomId
            """)
    void deleteByRoomId(@Param("roomId") Long roomId);

    List<InTouchRoomGroupBoardRow> findByRoomIdAndGroupIdOrderByRowIndexAsc(
            Long roomId,
            Long groupId
    );
}