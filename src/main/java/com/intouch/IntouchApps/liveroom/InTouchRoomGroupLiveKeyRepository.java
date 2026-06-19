package com.intouch.IntouchApps.liveroom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Collection;

public interface InTouchRoomGroupLiveKeyRepository
        extends JpaRepository<InTouchRoomGroupLiveKey, Long> {

    long countByRoomIdAndGroupId(Long roomId, Long groupId);

    long countByRoomIdAndGroupIdAndStatus(
            Long roomId,
            Long groupId,
            LiveKeyBuildStatus status
    );

    long countByRoomIdAndAssignedParticipantId(
            Long roomId,
            Long assignedParticipantId
    );

    long countByRoomId(Long roomId);

    @Query("""
                SELECT k
                FROM InTouchRoomGroupLiveKey k
                WHERE k.room.id = :roomId
                  AND k.assignedParticipant.id = :participantId
                  AND k.status = 'PLACED'
                ORDER BY k.currentRow ASC, k.currentColumn ASC
            """)
    List<InTouchRoomGroupLiveKey> findPlacedKeysForParticipant(
            @Param("roomId") Long roomId,
            @Param("participantId") Long participantId
    );
    @Query("""
        SELECT k
        FROM InTouchRoomGroupLiveKey k
        WHERE k.room.id = :roomId
          AND k.assignedParticipant.id = :participantId
          AND k.status = :status
        ORDER BY k.assignedOrder ASC
    """)
    List<InTouchRoomGroupLiveKey> findKeysForParticipantByStatus(
            @Param("roomId") Long roomId,
            @Param("participantId") Long participantId,
            @Param("status") LiveKeyBuildStatus status
    );
    long countByRoomIdAndAssignedParticipantMobileUserId(
            Long roomId,
            Integer mobileUserId
    );
    Optional<InTouchRoomGroupLiveKey>
    findFirstByRoom_IdAndGroup_IdAndAssignedParticipant_IdAndStatusOrderByAssignedOrderAsc(
            Long roomId,
            Long groupId,
            Long participantId,
            LiveKeyBuildStatus status
    );
    @Query("""
                SELECT k
                FROM InTouchRoomGroupLiveKey k
                WHERE k.room.id = :roomId
                  AND k.group.id = :groupId
                  AND k.status = 'PLACED'
                ORDER BY k.currentRow ASC, k.currentColumn ASC
            """)
    List<InTouchRoomGroupLiveKey> findOwnerPlacedBoardKeys(
            @Param("roomId") Long roomId,
            @Param("groupId") Long groupId
    );

    @Query("""
                SELECT k
                FROM InTouchRoomGroupLiveKey k
                WHERE k.room.id = :roomId
                  AND k.assignedParticipant.id = :participantId
                  AND k.status = 'NOT_STARTED'
                ORDER BY k.assignedOrder ASC
            """)
    List<InTouchRoomGroupLiveKey> findRemainingKeysForParticipant(
            @Param("roomId") Long roomId,
            @Param("participantId") Long participantId
    );

    @Query("""
                SELECT k
                FROM InTouchRoomGroupLiveKey k
                WHERE k.room.id = :roomId
                  AND k.group.id = :groupId
                  AND k.status <> 'REMOVED'
                  AND k.currentRow IS NOT NULL
                  AND k.currentColumn IS NOT NULL
                ORDER BY k.assignedOrder ASC
            """)
    List<InTouchRoomGroupLiveKey> findOwnerRemainingRemoveKeys(
            @Param("roomId") Long roomId,
            @Param("groupId") Long groupId
    );

    @Query("""
                SELECT k
                FROM InTouchRoomGroupLiveKey k
                JOIN k.assignedParticipant p
                JOIN p.mobileUser u
                WHERE k.room.id = :roomId
                  AND u.id = :currentUserId
                  AND p.status = 'ACTIVE'
                  AND k.status = 'PLACED'
                ORDER BY k.currentRow ASC, k.currentColumn ASC
            """)
    List<InTouchRoomGroupLiveKey> findMyPlacedKeysForBoard(
            @Param("roomId") Long roomId,
            @Param("currentUserId") Integer currentUserId
    );

    long countByRoomIdAndAssignedParticipantMobileUserIdAndStatus(
            Long roomId,
            Integer mobileUserId,
            LiveKeyBuildStatus status
    );

    long countByRoomIdAndAssignedParticipantMobileUserIdAndStatusIn(
            Long roomId,
            Integer mobileUserId,
            Collection<LiveKeyBuildStatus> statuses
    );

    List<InTouchRoomGroupLiveKey> findByRoomIdAndGroupIdOrderByAssignedOrderAsc(
            Long roomId,
            Long groupId
    );

    @Query("""
                SELECT k
                FROM InTouchRoomGroupLiveKey k
                JOIN k.assignedParticipant p
                JOIN p.mobileUser u
                WHERE k.id = :groupLiveKeyId
                  AND u.id = :currentUserId
                  AND p.status = 'ACTIVE'
            """)
    Optional<InTouchRoomGroupLiveKey> findAssignedKeyForCurrentUser(
            @Param("groupLiveKeyId") Long groupLiveKeyId,
            @Param("currentUserId") Integer currentUserId
    );

    List<InTouchRoomGroupLiveKey> findByRoomIdAndAssignedParticipantIdOrderByAssignedOrderAsc(
            Long roomId,
            Long assignedParticipantId
    );

    @Query("""
                SELECT k
                FROM InTouchRoomGroupLiveKey k
                JOIN k.assignedParticipant p
                JOIN p.mobileUser u
                WHERE k.room.id = :roomId
                  AND u.id = :currentUserId
                  AND p.status IN ('JOINED', 'ACTIVE')
                ORDER BY k.assignedOrder ASC
            """)
    List<InTouchRoomGroupLiveKey> findMyKeysForRoom(
            @Param("roomId") Long roomId,
            @Param("currentUserId") Integer currentUserId
    );

    @Query("""
                SELECT COUNT(k)
                FROM InTouchRoomGroupLiveKey k
                WHERE k.room.id = :roomId
                  AND k.group.id = :groupId
                  AND k.currentRow = k.targetRow
                  AND k.currentColumn = k.targetColumn
            """)
    long countCorrectlyPlacedKeys(
            @Param("roomId") Long roomId,
            @Param("groupId") Long groupId
    );
    @Query("""
                SELECT k
                FROM InTouchRoomGroupLiveKey k
                JOIN k.assignedParticipant p
                JOIN p.mobileUser u
                WHERE k.room.id = :roomId
                  AND u.id = :currentUserId
                  AND p.status = 'ACTIVE'
                  AND k.status = :status
                ORDER BY k.assignedOrder ASC
            """)
    List<InTouchRoomGroupLiveKey> findMyNextAvailableKeys(
            @Param("roomId") Long roomId,
            @Param("currentUserId") Integer currentUserId,
            @Param("status") LiveKeyBuildStatus status
    );

    @Query("""
                SELECT k
                FROM InTouchRoomGroupLiveKey k
                WHERE k.room.id = :roomId
                  AND k.group.id = :groupId
                ORDER BY k.targetRow ASC, k.targetColumn ASC
            """)
    List<InTouchRoomGroupLiveKey> findGroupTargetStructure(
            @Param("roomId") Long roomId,
            @Param("groupId") Long groupId
    );

    @Query("""
                SELECT CASE WHEN COUNT(k) > 0 THEN true ELSE false END
                FROM InTouchRoomGroupLiveKey k
                WHERE k.room.id = :roomId
                  AND k.group.id = :groupId
                  AND k.currentRow = :row
                  AND k.currentColumn = :column
                  AND k.status = 'PLACED'
            """)
    boolean existsPlacedKeyAtCell(
            @Param("roomId") Long roomId,
            @Param("groupId") Long groupId,
            @Param("row") Integer row,
            @Param("column") Integer column
    );

    @Query("""
                SELECT k
                FROM InTouchRoomGroupLiveKey k
                WHERE k.room.id = :roomId
                  AND k.group.id = :groupId
                  AND k.keyValue = :keyValue
                  AND k.currentRow IS NULL
                  AND k.currentColumn IS NULL
                  AND k.status = 'NOT_STARTED'
                ORDER BY k.targetRow ASC, k.targetColumn ASC
            """)
    List<InTouchRoomGroupLiveKey> findFirstAvailableMatchingValueTarget(
            @Param("roomId") Long roomId,
            @Param("groupId") Long groupId,
            @Param("keyValue") String keyValue
    );

    @Modifying
    @Query("""
                DELETE FROM InTouchRoomGroupLiveKey k
                WHERE k.room.id = :roomId
            """)
    void deleteByRoomId(@Param("roomId") Long roomId);

    void deleteByRoomIdAndGroupId(Long roomId, Long groupId);

    boolean existsByGroupIdAndCurrentRowAndCurrentColumnAndStatus(
            Long groupId,
            Integer currentRow,
            Integer currentColumn,
            LiveKeyBuildStatus status
    );

    boolean existsByRoomIdAndAssignedParticipantMobileUserId(
            Long roomId,
            Integer mobileUserId
    );

    @Query("""
                SELECT k
                FROM InTouchRoomGroupLiveKey k
                WHERE k.room.id = :roomId
                  AND k.group.id = :groupId
                  AND k.status = 'PLACED'
                ORDER BY k.currentRow ASC, k.currentColumn ASC
            """)
    List<InTouchRoomGroupLiveKey> findPlacedKeysForOwnerGroupBoard(
            @Param("roomId") Long roomId,
            @Param("groupId") Long groupId
    );

    long countByRoomIdAndGroupIdAndAssignedParticipantIdAndStatus(
            Long roomId,
            Long groupId,
            Long assignedParticipantId,
            LiveKeyBuildStatus status
    );

    @Query("""
                SELECT k
                FROM InTouchRoomGroupLiveKey k
                WHERE k.room.id = :roomId
                  AND k.group.id = :groupId
                  AND k.assignedParticipant.id = :participantId
                  AND k.status = 'NOT_STARTED'
                ORDER BY k.assignedOrder ASC
            """)
    List<InTouchRoomGroupLiveKey> findWaitingKeysForParticipantInGroup(
            @Param("roomId") Long roomId,
            @Param("groupId") Long groupId,
            @Param("participantId") Long participantId
    );

    @Query("""
                SELECT k
                FROM InTouchRoomGroupLiveKey k
                JOIN k.group g
                JOIN InTouchRoomGroupParticipant gp
                     ON gp.group.id = g.id
                JOIN gp.participant p
                JOIN p.mobileUser u
                WHERE k.room.id = :roomId
                  AND u.id = :currentUserId
                  AND p.status = 'ACTIVE'
                  AND (
                        k.removedByParticipant IS NULL
                        OR k.removedByParticipant.id <> p.id
                      )
                  AND k.currentRow IS NOT NULL
                  AND k.currentColumn IS NOT NULL
                ORDER BY k.currentRow ASC, k.currentColumn ASC
            """)
    List<InTouchRoomGroupLiveKey> findMyRemoveModeBoardKeys(
            @Param("roomId") Long roomId,
            @Param("currentUserId") Integer currentUserId
    );
    @Query("""
        SELECT k
        FROM InTouchRoomGroupLiveKey k
        WHERE k.room.id = :roomId
          AND k.group.id = :groupId
          AND k.assignedParticipant.id = :participantId
          AND k.status = :status
        ORDER BY k.assignedOrder ASC
    """)
    List<InTouchRoomGroupLiveKey> findWaitingKeysForParticipantInGroupByStatus(
            @Param("roomId") Long roomId,
            @Param("groupId") Long groupId,
            @Param("participantId") Long participantId,
            @Param("status") LiveKeyBuildStatus status
    );
}