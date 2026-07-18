package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.liveroom.dto.response.*;
import com.intouch.IntouchApps.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InTouchRoomMobileQueryService {

    private final InTouchRoomRepository roomRepository;
    private final InTouchRoomGroupLiveKeyRepository groupLiveKeyRepository;
    private final InTouchRoomParticipantRepository participantRepository;
    private final InTouchRoomLifecycleValidator lifecycleValidator;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public MobileRoomStatusResponse getParticipantRoomStatus(Long roomId) {
        InTouchRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found."));
        if (Boolean.TRUE.equals(room.getDeleted()) ||
                room.getStatus() == InTouchRoomStatus.DELETED) {
            throw new IllegalStateException("Deleted room status is unavailable.");
        }

        Integer currentUserId = securityUtils.getCurrentUserId();
        participantRepository.findByRoomIdAndMobileUserId(roomId, currentUserId)
                .orElseThrow(() -> new IllegalStateException(
                        "You are not assigned to this live room."
                ));

        return MobileRoomStatusResponse.builder()
                .roomId(roomId)
                .roomStatus(room.getStatus())
                .build();
    }

    @Transactional(readOnly = true)
    public MobileRoomWorkResponse getMyRoomWork(Long roomId) {
        Integer currentUserId = securityUtils.getCurrentUserId();

        InTouchRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found."));

        List<InTouchRoomGroupLiveKey> keys =
                groupLiveKeyRepository.findMyKeysForRoom(roomId, currentUserId);

        if (keys.isEmpty()) {
            throw new IllegalArgumentException(
                    "No assigned keys found for current user in this room."
            );
        }

        InTouchRoomGroup group = keys.get(0).getGroup();

        List<MobileLiveKeyResponse> myKeys = keys.stream()
                .map(this::toMobileLiveKeyResponse)
                .toList();

        return MobileRoomWorkResponse.builder()
                .roomId(room.getId())
                .roomCode(room.getRoomCode())
                .title(room.getTitle())
                .buildMode(room.getBuildMode())
                .placementStrategy(room.getPlacementStrategy())
                .status(room.getStatus())
                .groupId(group.getId())
                .groupName(group.getName())
                .targetStructure(room.getTargetStructure())
                .myKeys(myKeys)
                .build();
    }
    @Transactional(readOnly = true)
    public MobileMyBoardResponse getMyBoard(Long roomId) {
        Integer currentUserId = securityUtils.getCurrentUserId();
        InTouchRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found."));
        List<InTouchRoomGroupLiveKey> boardKeys;

        if (room.getBuildMode() == LiveRoomBuildMode.REMOVE_KEYS) {
            boardKeys = groupLiveKeyRepository.findMyRemoveModeBoardKeys(
                    roomId,
                    currentUserId
            );
        } else {
            boardKeys = groupLiveKeyRepository.findMyPlacedKeysForBoard(
                    roomId,
                    currentUserId
            );
        }
        return buildBoardResponse(roomId, boardKeys);
    }

    MobileMyBoardResponse getCompletedReviewBoard(InTouchRoom room) {
        Integer currentUserId = securityUtils.getCurrentUserId();
        List<InTouchRoomGroupLiveKey> boardKeys;

        if (room.getBuildMode() == LiveRoomBuildMode.REMOVE_KEYS) {
            boardKeys = groupLiveKeyRepository
                    .findMyRemoveModeBoardKeysForCompletedReview(
                            room.getId(), currentUserId);
        } else {
            boardKeys = groupLiveKeyRepository
                    .findMyPlacedKeysForCompletedReview(
                            room.getId(), currentUserId);
        }

        return buildBoardResponse(room.getId(), boardKeys);
    }

    private MobileMyBoardResponse buildBoardResponse(
            Long roomId,
            List<InTouchRoomGroupLiveKey> boardKeys
    ) {
        if (boardKeys.isEmpty()) {
            return MobileMyBoardResponse.builder()
                    .roomId(roomId)
                    .rows(List.of())
                    .build();
        }

        InTouchRoomParticipant participant =
                boardKeys.get(0).getAssignedParticipant();

        InTouchRoomGroup group =
                boardKeys.get(0).getGroup();

        Map<Integer, List<InTouchRoomGroupLiveKey>> keysByRow =
                boardKeys.stream()
                        .collect(Collectors.groupingBy(
                                InTouchRoomGroupLiveKey::getCurrentRow,
                                TreeMap::new,
                                Collectors.toList()
                        ));

        List<MobileBoardRowResponse> rows =
                keysByRow.entrySet()
                        .stream()
                        .map(entry -> {
                            Integer rowIndex = entry.getKey();
                            List<InTouchRoomGroupLiveKey> rowKeys = entry.getValue();

                            String keyFamilyId = rowKeys.get(0).getKeyFamilyId();

                            List<MobileBoardCellResponse> cells =
                                    rowKeys.stream()
                                            .map(k -> MobileBoardCellResponse.builder()
                                                    .columnIndex(k.getCurrentColumn())
                                                    .keyValue(k.getKeyValue())
                                                    .groupLiveKeyId(k.getId())
                                                    .status(k.getStatus())
                                                    .build())
                                            .toList();

                            return MobileBoardRowResponse.builder()
                                    .rowIndex(rowIndex)
                                    .keyFamilyId(keyFamilyId)
                                    .cells(cells)
                                    .build();
                        })
                        .toList();

        return MobileMyBoardResponse.builder()
                .roomId(roomId)
                .groupId(group.getId())
                .groupName(group.getName())
                .participantId(participant.getId())
                .participantDisplayName(participant.getDisplayName())
                .rows(rows)
                .build();
    }
    private MobileLiveKeyResponse toMobileLiveKeyResponse(InTouchRoomGroupLiveKey key) {
       return MobileLiveKeyResponse.builder()
                .id(key.getId())
                .keyValue(key.getKeyValue())
                .keyType(key.getKeyType())
                .keyFamilyId(key.getKeyFamilyId())
                .assignedOrder(key.getAssignedOrder())
                .currentRow(key.getCurrentRow())
                .currentColumn(key.getCurrentColumn())
                .targetRow(key.getTargetRow())
                .targetColumn(key.getTargetColumn())
                .status(key.getStatus())
                .build();
    }
    @Transactional(readOnly = true)
    public MobileNextKeyResponse getNextKeyForCurrentParticipant(Long roomId) {
        Integer currentUserId = securityUtils.getCurrentUserId();

        InTouchRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found."));

        lifecycleValidator.ensureGameplayAllowed(room);

        InTouchRoomParticipant participant = participantRepository
                .findByRoomIdAndMobileUserId(roomId, currentUserId)
                .orElseThrow(() -> new IllegalStateException(
                        "You are not assigned to this live room."
                ));

        if (participant.getStatus() != ParticipantStatus.ACTIVE ||
                !Boolean.TRUE.equals(participant.getActiveInRoom())) {
            throw new IllegalStateException("Participant is not active in this room.");
        }
        return buildNextKeyResponse(room, currentUserId);
    }

    MobileNextKeyResponse buildNextKeyResponse(
            InTouchRoom room,
            Integer currentUserId
    ) {
        Long roomId = room.getId();
        LiveKeyBuildStatus remainingStatus =
                room.getBuildMode() == LiveRoomBuildMode.REMOVE_KEYS
                        ? LiveKeyBuildStatus.IN_PROGRESS
                        : LiveKeyBuildStatus.NOT_STARTED;

        List<InTouchRoomGroupLiveKey> nextKeys =
                groupLiveKeyRepository.findMyNextAvailableKeys(
                        roomId,
                        currentUserId,
                        remainingStatus
                );
        long totalAssigned =
                groupLiveKeyRepository.countByRoomIdAndAssignedParticipantMobileUserId(
                        roomId,
                        currentUserId
                );

        LiveKeyBuildStatus completedStatus =
                room.getBuildMode() == LiveRoomBuildMode.REMOVE_KEYS
                        ? LiveKeyBuildStatus.REMOVED
                        : LiveKeyBuildStatus.PLACED;

        long remaining =
                groupLiveKeyRepository.countByRoomIdAndAssignedParticipantMobileUserIdAndStatus(
                        roomId,
                        currentUserId,
                        remainingStatus
                );

        long completed =
                groupLiveKeyRepository.countByRoomIdAndAssignedParticipantMobileUserIdAndStatus(
                        roomId,
                        currentUserId,
                        completedStatus
                );

        if (nextKeys.isEmpty()) {
            return MobileNextKeyResponse.builder()
                    .roomId(roomId)
                    .roomTitle(room.getTitle())
                    .roomStatus(room.getStatus())
                    .buildMode(room.getBuildMode())
                    .noMoreKeys(true)
                    .totalAssignedKeys(totalAssigned)
                    .completedAssignedKeys(completed)
                    .remainingAssignedKeys(0)
                    .nextKey(null)
                    .build();
        }

        InTouchRoomGroupLiveKey nextKey = nextKeys.get(0);
       return MobileNextKeyResponse.builder()
                .roomId(roomId)
                .roomTitle(room.getTitle())
                .keyFamilyId(nextKey.getKeyFamilyId())
                .roomStatus(room.getStatus())
                .buildMode(room.getBuildMode())
                .groupId(nextKey.getGroup().getId())
                .groupName(nextKey.getGroup().getName())
                .nextKey(toMobileLiveKeyResponse(nextKey))
                .noMoreKeys(false)
                .totalAssignedKeys(totalAssigned)
                .completedAssignedKeys(completed)
                .remainingAssignedKeys(remaining)
                .build();
    }

}
