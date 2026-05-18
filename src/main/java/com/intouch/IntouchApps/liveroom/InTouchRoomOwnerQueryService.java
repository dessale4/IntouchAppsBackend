package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.liveroom.dto.response.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InTouchRoomOwnerQueryService {

    private final InTouchRoomRepository roomRepository;
    private final InTouchRoomGroupRepository groupRepository;
    private final InTouchRoomAccessValidator accessValidator;
    private final InTouchRoomGroupLiveKeyRepository groupLiveKeyRepository;
    private final InTouchRoomGroupParticipantRepository groupParticipantRepository;
    private final InTouchRoomParticipantRepository participantRepository;
    @Transactional(readOnly = true)
    public OwnerLiveRoomGroupDetailResponse getGroupDetail(
            Long roomId,
            Long groupId
    ) {
        InTouchRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found."));

        accessValidator.ensureRoomOwner(room);

        InTouchRoomGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found."));

        if (!group.getRoom().getId().equals(roomId)) {
            throw new IllegalStateException("Group does not belong to this room.");
        }

        List<InTouchRoomGroupLiveKey> placedKeys =
                groupLiveKeyRepository.findPlacedKeysForOwnerGroupBoard(roomId, groupId);

        Map<Integer, List<InTouchRoomGroupLiveKey>> keysByRow =
                placedKeys.stream()
                        .collect(Collectors.groupingBy(
                                InTouchRoomGroupLiveKey::getCurrentRow,
                                TreeMap::new,
                                Collectors.toList()
                        ));

        List<OwnerLiveRoomBoardRowResponse> rows =
                keysByRow.entrySet().stream()
                        .map(entry -> {
                            List<InTouchRoomGroupLiveKey> rowKeys = entry.getValue();

                            return OwnerLiveRoomBoardRowResponse.builder()
                                    .rowIndex(entry.getKey())
                                    .keyFamilyId(rowKeys.get(0).getKeyFamilyId())
                                    .cells(rowKeys.stream()
                                            .map(this::toOwnerBoardCell)
                                            .toList())
                                    .build();
                        })
                        .toList();

        List<InTouchRoomGroupParticipant> assignments =
                groupParticipantRepository.findActiveAssignmentsByRoomIdAndGroupId(
                        roomId,
                        groupId
                );

        List<OwnerLiveRoomParticipantSummaryResponse> participants =
                assignments.stream()
                        .map(gp -> toParticipantSummary(roomId, groupId, gp.getParticipant()))
                        .toList();

        long totalKeys =
                groupLiveKeyRepository.countByRoomIdAndGroupId(
                        roomId,
                        groupId
                );

        long completedKeys =
                groupLiveKeyRepository.countByRoomIdAndGroupIdAndStatus(
                        roomId,
                        groupId,
                        LiveKeyBuildStatus.PLACED
                );

        double progressPercent =
                totalKeys == 0
                        ? 0.0
                        : (completedKeys * 100.0) / totalKeys;

        return OwnerLiveRoomGroupDetailResponse.builder()
                .roomId(roomId)
                .groupId(group.getId())
                .groupName(group.getName())
                .progressPercent(progressPercent)
                .errorCount(group.getErrorCount())
                .score(group.getScore())
                .completedPatternCount(group.getCompletedPatternCount())
                .rows(rows)
                .participants(participants)
                .build();
    }
    private OwnerLiveRoomBoardCellResponse toOwnerBoardCell(
            InTouchRoomGroupLiveKey key
    ) {
        InTouchRoomParticipant participant = key.getAssignedParticipant();

        return OwnerLiveRoomBoardCellResponse.builder()
                .keyId(key.getId())
                .columnIndex(key.getCurrentColumn())
                .keyValue(key.getKeyValue())
                .keyFamilyId(key.getKeyFamilyId())
                .status(key.getStatus())
                .participantId(participant != null ? participant.getId() : null)
                .participantDisplayName(
                        participant != null ? participant.getDisplayName() : null
                )
                .build();
    }
    private OwnerLiveRoomParticipantSummaryResponse toParticipantSummary(
            Long roomId,
            Long groupId,
            InTouchRoomParticipant participant
    ) {
        long completed =
                groupLiveKeyRepository
                        .countByRoomIdAndGroupIdAndAssignedParticipantIdAndStatus(
                                roomId,
                                groupId,
                                participant.getId(),
                                LiveKeyBuildStatus.PLACED
                        );

        long remaining =
                groupLiveKeyRepository
                        .countByRoomIdAndGroupIdAndAssignedParticipantIdAndStatus(
                                roomId,
                                groupId,
                                participant.getId(),
                                LiveKeyBuildStatus.NOT_STARTED
                        );

        List<InTouchRoomGroupLiveKey> waitingKeys =
                groupLiveKeyRepository.findWaitingKeysForParticipantInGroup(
                        roomId,
                        groupId,
                        participant.getId()
                );

        MobileLiveKeyResponse waitingKey =
                waitingKeys.isEmpty()
                        ? null
                        : toMobileLiveKeyResponse(waitingKeys.get(0));

        return OwnerLiveRoomParticipantSummaryResponse.builder()
                .participantId(participant.getId())
                .displayName(participant.getDisplayName())
                .participantCode(participant.getParticipantCode())
                .status(participant.getStatus())
                .completedKeys(completed)
                .remainingKeys(remaining)
                .waitingKey(waitingKey)
                .build();
    }
    @Transactional(readOnly = true)
    public OwnerLiveRoomParticipantDetailResponse getParticipantDetail(
            Long roomId,
            Long participantId
    ) {
        InTouchRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found."));

        accessValidator.ensureRoomOwner(room);

        InTouchRoomParticipant participant =
                participantRepository.findById(participantId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Participant not found."
                        ));

        if (!participant.getRoom().getId().equals(roomId)) {
            throw new IllegalStateException(
                    "Participant does not belong to this room."
            );
        }

        InTouchRoomGroupParticipant assignment =
                groupParticipantRepository
                        .findByRoomIdAndParticipantId(roomId, participantId)
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException(
                                "Participant is not assigned to a group."
                        ));

        InTouchRoomGroup group = assignment.getGroup();

        long totalAssigned =
                groupLiveKeyRepository.countByRoomIdAndAssignedParticipantId(
                        roomId,
                        participantId
                );

        long completed =
                groupLiveKeyRepository
                        .countByRoomIdAndGroupIdAndAssignedParticipantIdAndStatus(
                                roomId,
                                group.getId(),
                                participantId,
                                LiveKeyBuildStatus.PLACED
                        );

        long remaining =
                groupLiveKeyRepository
                        .countByRoomIdAndGroupIdAndAssignedParticipantIdAndStatus(
                                roomId,
                                group.getId(),
                                participantId,
                                LiveKeyBuildStatus.NOT_STARTED
                        );

        List<InTouchRoomGroupLiveKey> placedKeys =
                groupLiveKeyRepository.findPlacedKeysForParticipant(
                        roomId,
                        participantId
                );

        List<InTouchRoomGroupLiveKey> remainingKeys =
                groupLiveKeyRepository.findRemainingKeysForParticipant(
                        roomId,
                        participantId
                );

        MobileLiveKeyResponse waitingKey =
                remainingKeys.isEmpty()
                        ? null
                        : toMobileLiveKeyResponse(remainingKeys.get(0));

        return OwnerLiveRoomParticipantDetailResponse.builder()
                .roomId(roomId)
                .groupId(group.getId())
                .groupName(group.getName())
                .participantId(participant.getId())
                .displayName(participant.getDisplayName())
                .participantCode(participant.getParticipantCode())
                .status(participant.getStatus())
                .completedKeys(completed)
                .remainingKeys(remaining)
                .totalAssignedKeys(totalAssigned)
                .waitingKey(waitingKey)
                .placedKeys(
                        placedKeys.stream()
                                .map(this::toMobileLiveKeyResponse)
                                .toList()
                )
                .remainingAssignedKeys(
                        remainingKeys.stream()
                                .limit(20)
                                .map(this::toMobileLiveKeyResponse)
                                .toList()
                )
                .build();
    }
    private MobileLiveKeyResponse toMobileLiveKeyResponse(
            InTouchRoomGroupLiveKey key
    ) {
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
}
