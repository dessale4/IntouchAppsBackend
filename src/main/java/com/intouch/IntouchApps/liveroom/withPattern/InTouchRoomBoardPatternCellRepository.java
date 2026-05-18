package com.intouch.IntouchApps.liveroom.withPattern;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InTouchRoomBoardPatternCellRepository
        extends JpaRepository<InTouchRoomBoardPatternCell, Long> {

    List<InTouchRoomBoardPatternCell> findByPatternId(Long patternId);

    @Query("""
                SELECT pc
                FROM InTouchRoomBoardPatternCell pc
                JOIN pc.pattern p
                WHERE p.room.id = :roomId
                  AND pc.targetRow = :targetRow
                  AND pc.targetColumn = :targetColumn
                  AND p.active = true
            """)
    List<InTouchRoomBoardPatternCell> findActivePatternCellsForRoomCell(
            @Param("roomId") Long roomId,
            @Param("targetRow") Integer targetRow,
            @Param("targetColumn") Integer targetColumn
    );
}
