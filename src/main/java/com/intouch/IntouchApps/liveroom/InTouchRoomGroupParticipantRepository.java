package com.intouch.IntouchApps.liveroom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InTouchRoomGroupParticipantRepository
        extends JpaRepository<InTouchRoomGroupParticipant, Long> {

    List<InTouchRoomGroupParticipant> findByRoomId(Long roomId);

    long countByRoomIdAndGroupId(Long roomId, Long groupId);

    List<InTouchRoomGroupParticipant> findByGroupId(Long groupId);

    List<InTouchRoomGroupParticipant> findByRoomIdAndParticipantId(
            Long roomId,
            Long participantId
    );

    long countByRoomId(Long roomId);

    @Query("""
                SELECT COUNT(DISTINCT gp.participant.id)
                FROM InTouchRoomGroupParticipant gp
                WHERE gp.room.id = :roomId
                  AND gp.participant.status <> 'REMOVED'
                  AND gp.group.room.id = :roomId
            """)
    long countDistinctAssignedActiveParticipants(@Param("roomId") Long roomId);

    @Query("""
                SELECT gp
                FROM InTouchRoomGroupParticipant gp
                JOIN FETCH gp.group g
                JOIN FETCH gp.participant p
                LEFT JOIN FETCH p.mobileUser u
                WHERE gp.room.id = :roomId
                ORDER BY g.sortOrder ASC, p.displayName ASC
            """)
    List<InTouchRoomGroupParticipant> findAssignmentsForParticipantAccess(
            @Param("roomId") Long roomId
    );

    boolean existsByRoomIdAndParticipantId(Long roomId, Long participantId);

    @Modifying
    @Query("""
                DELETE FROM InTouchRoomGroupParticipant gp
                WHERE gp.room.id = :roomId
                  AND gp.group.id = :groupId
            """)
    void deleteByRoomIdAndGroupId(
            @Param("roomId") Long roomId,
            @Param("groupId") Long groupId
    );

    @Modifying
    @Query("""
                DELETE FROM InTouchRoomGroupParticipant gp
                WHERE gp.room.id = :roomId
            """)
    void deleteByRoomId(@Param("roomId") Long roomId);

    @Query("""
                SELECT gp.participant
                FROM InTouchRoomGroupParticipant gp
                WHERE gp.group.id = :groupId
                ORDER BY gp.participant.id ASC
            """)
    List<InTouchRoomParticipant> findParticipantsByGroupId(
            @Param("groupId") Long groupId
    );

    @Query("""
                SELECT gp
                FROM InTouchRoomGroupParticipant gp
                WHERE gp.room.id = :roomId
                ORDER BY gp.group.sortOrder ASC, gp.participant.displayName ASC
            """)
    List<InTouchRoomGroupParticipant> findRoomAssignments(
            @Param("roomId") Long roomId
    );

    @Query("""
                SELECT COUNT(DISTINCT gp.participant.id)
                FROM InTouchRoomGroupParticipant gp
                WHERE gp.room.id = :roomId
            """)
    long countDistinctAssignedParticipantsByRoomId(@Param("roomId") Long roomId);

    @Query("""
                SELECT gp
                FROM InTouchRoomGroupParticipant gp
                JOIN FETCH gp.participant p
                WHERE gp.room.id = :roomId
                  AND gp.group.id = :groupId
                  AND p.status <> 'REMOVED'
                ORDER BY p.displayName ASC
            """)
    List<InTouchRoomGroupParticipant> findActiveAssignmentsByRoomIdAndGroupId(
            @Param("roomId") Long roomId,
            @Param("groupId") Long groupId
    );
}
