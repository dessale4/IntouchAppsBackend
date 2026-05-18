package com.intouch.IntouchApps.liveroom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InTouchRoomParticipantRepository
        extends JpaRepository<InTouchRoomParticipant, Long> {
    @Query("""
                SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
                FROM InTouchRoomParticipant p
                JOIN p.room r
                WHERE p.mobileUser.id = :userId
                  AND p.status IN ('JOINED', 'ACTIVE')
                  AND r.status = 'STARTED'
            """)
    boolean existsActiveParticipantInActiveRoom(@Param("userId") Integer userId);

    @Modifying
    @Query("""
                UPDATE InTouchRoomParticipant p
                SET p.status = 'INVITED',
                    p.activeInRoom = false,
                    p.activatedAt = null
                WHERE p.room.id = :roomId
                  AND p.status <> 'REMOVED'
                  AND p.mobileUser IS NULL
            """)
    void resetUnclaimedParticipantsAfterRoomReset(@Param("roomId") Long roomId);

    @Modifying
    @Query("""
                UPDATE InTouchRoomParticipant p
                SET p.status = 'INVITED',
                    p.mobileUser = null,
                    p.activeInRoom = false,
                    p.claimedAt = null,
                    p.activatedAt = null
                WHERE p.room.id = :roomId
            """)
    void resetParticipantsAfterRoomReset(@Param("roomId") Long roomId);

    @Query("""
                SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
                FROM InTouchRoomParticipant p
                JOIN p.room r
                WHERE p.mobileUser.id = :userId
                  AND p.status IN ('JOINED', 'ACTIVE')
                  AND r.status = 'STARTED'
                  AND r.id <> :currentRoomId
            """)
    boolean existsActiveParticipantInOtherActiveRoom(
            @Param("userId") Integer userId,
            @Param("currentRoomId") Long currentRoomId
    );

    @Query("""
                SELECT p
                FROM InTouchRoomParticipant p
                WHERE p.room.id = :roomId
                  AND p.mobileUser IS NULL
                  AND p.status <> 'REMOVED'
                ORDER BY p.displayName ASC
            """)
    List<InTouchRoomParticipant> findUnclaimedParticipantsByRoomId(
            @Param("roomId") Long roomId
    );

    long countByRoomId(Long roomId);

    boolean existsByRoomIdAndParticipantCode(Long roomId, String participantCode);

    List<InTouchRoomParticipant> findByRoomId(Long roomId);

    List<InTouchRoomParticipant> findByRoomIdAndStatusNot(Long roomId, ParticipantStatus status);

    @Query("""
                SELECT p
                FROM InTouchRoomParticipant p
                JOIN p.room r
                WHERE r.roomCode = :roomCode
                  AND p.participantCode = :participantCode
            """)
    Optional<InTouchRoomParticipant> findByRoomCodeAndParticipantCode(
            @Param("roomCode") String roomCode,
            @Param("participantCode") String participantCode
    );

    long countByRoomIdAndStatusNot(Long roomId, ParticipantStatus status);

    boolean existsByRoomIdAndDisplayNameIgnoreCaseAndStatusNot(
            Long roomId,
            String displayName,
            ParticipantStatus status
    );
}
