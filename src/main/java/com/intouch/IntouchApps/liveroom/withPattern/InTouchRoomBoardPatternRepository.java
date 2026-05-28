package com.intouch.IntouchApps.liveroom.withPattern;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InTouchRoomBoardPatternRepository
        extends JpaRepository<InTouchRoomBoardPattern, Long> {

    @Modifying
    @Query("""
        DELETE FROM InTouchRoomBoardPattern p
        WHERE p.room.id = :roomId
    """)
    void deleteByRoomId(@Param("roomId") Long roomId);

    @Query("""
    SELECT p
    FROM InTouchRoomBoardPattern p
    WHERE p.room.id = :roomId
""")
    List<InTouchRoomBoardPattern> findByRoomId(@Param("roomId") Long roomId);
}