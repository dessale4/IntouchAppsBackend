package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.liveroom.dto.response.*;
import com.intouch.IntouchApps.user.User;
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

        List<InTouchRoomGroupLiveKey> boardKeys =
                room.getBuildMode() == LiveRoomBuildMode.REMOVE_KEYS
                        ? groupLiveKeyRepository.findOwnerRemainingRemoveKeys(roomId, groupId)
                        : groupLiveKeyRepository.findPlacedKeysForOwnerGroupBoard(roomId, groupId);

        Map<Integer, List<InTouchRoomGroupLiveKey>> keysByRow =
                boardKeys.stream()
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
                        .map(gp -> toParticipantSummary(room, groupId, gp.getParticipant()))
                        .toList();

        long totalKeys =
                groupLiveKeyRepository.countByRoomIdAndGroupId(
                        roomId,
                        groupId
                );
        LiveKeyBuildStatus groupCompletedStatus =
                room.getBuildMode() == LiveRoomBuildMode.REMOVE_KEYS
                        ? LiveKeyBuildStatus.REMOVED
                        : LiveKeyBuildStatus.PLACED;
        long completedKeys =
                groupLiveKeyRepository.countByRoomIdAndGroupIdAndStatus(
                        roomId,
                        groupId,
                        groupCompletedStatus
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
            InTouchRoom room,
            Long groupId,
            InTouchRoomParticipant participant
    ) {
        LiveKeyBuildStatus completedStatus =
                room.getBuildMode() == LiveRoomBuildMode.REMOVE_KEYS
                        ? LiveKeyBuildStatus.REMOVED
                        : LiveKeyBuildStatus.PLACED;

        LiveKeyBuildStatus remainingStatus =
                room.getBuildMode() == LiveRoomBuildMode.REMOVE_KEYS
                        ? LiveKeyBuildStatus.IN_PROGRESS
                        : LiveKeyBuildStatus.NOT_STARTED;

        long completed =
                groupLiveKeyRepository
                        .countByRoomIdAndGroupIdAndAssignedParticipantIdAndStatus(
                                room.getId(),
                                groupId,
                                participant.getId(),
                                completedStatus
                        );

        long remaining =
                groupLiveKeyRepository
                        .countByRoomIdAndGroupIdAndAssignedParticipantIdAndStatus(
                                room.getId(),
                                groupId,
                                participant.getId(),
                                remainingStatus
                        );

        List<InTouchRoomGroupLiveKey> waitingKeys =
                groupLiveKeyRepository.findWaitingKeysForParticipantInGroupByStatus(
                        room.getId(),
                        groupId,
                        participant.getId(),
                        remainingStatus
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
                .mobileUserId(
                        participant.getMobileUser() == null
                                ? null
                                : participant.getMobileUser().getId()
                )
                .mobileUsername(
                        participant.getMobileUser() == null
                                ? null
                                : participant.getMobileUser().getUserName()
                )
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
        LiveKeyBuildStatus completedStatus =
                room.getBuildMode() == LiveRoomBuildMode.REMOVE_KEYS
                        ? LiveKeyBuildStatus.REMOVED
                        : LiveKeyBuildStatus.PLACED;

        LiveKeyBuildStatus remainingStatus =
                room.getBuildMode() == LiveRoomBuildMode.REMOVE_KEYS
                        ? LiveKeyBuildStatus.IN_PROGRESS
                        : LiveKeyBuildStatus.NOT_STARTED;

        long completed =
                groupLiveKeyRepository
                        .countByRoomIdAndGroupIdAndAssignedParticipantIdAndStatus(
                                roomId,
                                group.getId(),
                                participantId,
                                completedStatus
                        );

        long remaining =
                groupLiveKeyRepository
                        .countByRoomIdAndGroupIdAndAssignedParticipantIdAndStatus(
                                roomId,
                                group.getId(),
                                participantId,
                                remainingStatus
                        );
        List<InTouchRoomGroupLiveKey> completedKeys =
                groupLiveKeyRepository.findKeysForParticipantByStatus(
                        roomId,
                        participantId,
                        completedStatus
                );

        List<InTouchRoomGroupLiveKey> remainingKeys =
                groupLiveKeyRepository.findKeysForParticipantByStatus(
                        roomId,
                        participantId,
                        remainingStatus
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
                        completedKeys.stream()
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

    @Transactional(readOnly = true)
    public LiveRoomGroupBoardResponse getGroupBoard(Long roomId, Long groupId) {
        InTouchRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found."));

        accessValidator.ensureRoomOwnerOrAdmin(room);

        InTouchRoomGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found."));

        if (!group.getRoom().getId().equals(roomId)) {
            throw new IllegalStateException("Group does not belong to this room.");
        }

        List<InTouchRoomGroupLiveKey> keys =
                room.getBuildMode() == LiveRoomBuildMode.REMOVE_KEYS
                        ? groupLiveKeyRepository.findOwnerRemainingRemoveKeys(roomId, groupId)
                        : groupLiveKeyRepository.findOwnerPlacedBoardKeys(roomId, groupId);

        List<LiveRoomGroupBoardCellResponse> cells = keys.stream()
                .map(k -> LiveRoomGroupBoardCellResponse.builder()
                        .groupLiveKeyId(k.getId())
                        .keyValue(k.getKeyValue())
                        .keyFamilyId(k.getKeyFamilyId())
                        .rowIndex(k.getCurrentRow())
                        .columnIndex(k.getCurrentColumn())
                        .status(k.getStatus())
                        .build())
                .toList();

        return LiveRoomGroupBoardResponse.builder()
                .roomId(roomId)
                .groupId(groupId)
                .groupName(group.getName())
                .buildMode(room.getBuildMode())
                .cells(cells)
                .build();
    }
    @Transactional(readOnly = true)
    public LiveRoomParticipantAccessResponse getParticipantAccess(Long roomId) {
        InTouchRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found."));

        accessValidator.ensureRoomOwnerOrAdmin(room);

        List<InTouchRoomParticipant> participants =
                participantRepository.findByRoomIdWithMobileUser(roomId);

        List<InTouchRoomGroupParticipant> assignments =
                groupParticipantRepository.findAssignmentsForParticipantAccess(roomId);

        Map<Long, InTouchRoomGroup> groupByParticipantId =
                assignments.stream()
                        .collect(Collectors.toMap(
                                gp -> gp.getParticipant().getId(),
                                InTouchRoomGroupParticipant::getGroup,
                                (a, b) -> a
                        ));

        List<LiveRoomParticipantAccessRowResponse> rows =
                participants.stream()
                        .map(participant -> {
                            InTouchRoomGroup group =
                                    groupByParticipantId.get(participant.getId());

                            User mobileUser = participant.getMobileUser();
                            int releasableUnfinishedKeyCount = 0;
                            int releasedToPoolKeyCount = 0;

                            if (group != null) {
                                LiveKeyBuildStatus unfinishedStatus =
                                        room.getBuildMode() == LiveRoomBuildMode.REMOVE_KEYS
                                                ? LiveKeyBuildStatus.IN_PROGRESS
                                                : LiveKeyBuildStatus.NOT_STARTED;
                                releasableUnfinishedKeyCount = Math.toIntExact(
                                        groupLiveKeyRepository
                                                .countByRoomIdAndGroupIdAndAssignedParticipantIdAndAssignmentStateAndStatus(
                                                        roomId,
                                                        group.getId(),
                                                        participant.getId(),
                                                        LiveKeyAssignmentState.ASSIGNED,
                                                        unfinishedStatus
                                                )
                                );
                                releasedToPoolKeyCount = Math.toIntExact(
                                        groupLiveKeyRepository
                                                .countByRoomIdAndGroupIdAndReleasedFromParticipantId(
                                                        roomId,
                                                        group.getId(),
                                                        participant.getId()
                                                )
                                );
                            }

                            return LiveRoomParticipantAccessRowResponse.builder()
                                    .participantId(participant.getId())
                                    .participantDisplayName(participant.getDisplayName())
                                    .participantCode(participant.getParticipantCode())
                                    .status(participant.getStatus())
                                    .groupId(group == null ? null : group.getId())
                                    .groupName(group == null ? "Unassigned" : group.getName())
                                    .mobileUserId(mobileUser == null ? null : mobileUser.getId())
                                    .mobileUsername(mobileUser == null ? null : mobileUser.getUserName())
                                    .claimedAt(participant.getClaimedAt())
                                    .releasableUnfinishedKeyCount(releasableUnfinishedKeyCount)
                                    .releasedToPoolKeyCount(releasedToPoolKeyCount)
                                    .build();
                        })
                        .toList();

        return LiveRoomParticipantAccessResponse.builder()
                .roomId(room.getId())
                .roomCode(room.getRoomCode())
                .roomTitle(room.getTitle())
                .roomStatus(room.getStatus())
                .participants(rows)
                .build();
    }
}
