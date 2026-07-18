package com.intouch.IntouchApps.liveroom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InTouchRoomRepository extends JpaRepository<InTouchRoom, Long> {
    boolean existsByRoomCode(String roomCode);
    Optional<InTouchRoom> findByRoomCode(String roomCode);
    List<InTouchRoom> findByOwnerIdAndDeletedFalseOrderByCreatedAtDesc(Integer ownerId);

    boolean existsByOwnerIdAndTitleIgnoreCaseAndDeletedFalse(
            Integer ownerId,
            String title
    );
    @Query("""
    SELECT r
    FROM InTouchRoom r
    JOIN r.owner o
    WHERE LOWER(o.userName) = LOWER(:ownerUsername)
      AND (:roomCode IS NULL OR :roomCode = '' OR r.roomCode = :roomCode)
    ORDER BY r.updatedAt DESC
""")
    List<InTouchRoom> findRoomsForManager(
            @Param("ownerUsername") String ownerUsername,
            @Param("roomCode") String roomCode
    );
}

