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
    private final SecurityUtils securityUtils;

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

        List<InTouchRoomGroupLiveKey> placedKeys =
                groupLiveKeyRepository.findMyPlacedKeysForBoard(
                        roomId,
                        currentUserId
                );

        if (placedKeys.isEmpty()) {
            return MobileMyBoardResponse.builder()
                    .roomId(roomId)
                    .rows(List.of())
                    .build();
        }

        InTouchRoomParticipant participant =
                placedKeys.get(0).getAssignedParticipant();

        InTouchRoomGroup group =
                placedKeys.get(0).getGroup();

        Map<Integer, List<InTouchRoomGroupLiveKey>> keysByRow =
                placedKeys.stream()
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

        if (room.getStatus() != InTouchRoomStatus.STARTED &&
                room.getStatus() != InTouchRoomStatus.COMPLETED) {
            throw new IllegalStateException("Live room has not started.");
        }

        boolean participantExists =
                groupLiveKeyRepository.existsByRoomIdAndAssignedParticipantMobileUserId(
                        roomId,
                        currentUserId
                );

        if (!participantExists) {
            throw new IllegalStateException(
                    "You are not assigned to this live room."
            );
        }

        List<InTouchRoomGroupLiveKey> nextKeys =
                groupLiveKeyRepository.findMyNextAvailableKeys(roomId, currentUserId);

        long totalAssigned =
                groupLiveKeyRepository.countByRoomIdAndAssignedParticipantMobileUserId(
                        roomId,
                        currentUserId
                );

        long remaining =
                groupLiveKeyRepository.countByRoomIdAndAssignedParticipantMobileUserIdAndStatus(
                        roomId,
                        currentUserId,
                        LiveKeyBuildStatus.NOT_STARTED
                );

        LiveKeyBuildStatus completedStatus =
                room.getBuildMode() == LiveRoomBuildMode.REMOVE_KEYS
                        ? LiveKeyBuildStatus.REMOVED
                        : LiveKeyBuildStatus.PLACED;

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