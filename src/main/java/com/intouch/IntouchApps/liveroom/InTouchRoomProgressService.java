package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.liveroom.dto.response.CurrentParticipantKeyDto;
import com.intouch.IntouchApps.liveroom.dto.response.InTouchRoomGroupProgressDto;
import com.intouch.IntouchApps.liveroom.dto.response.InTouchRoomProgressDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InTouchRoomProgressService {

    private final InTouchRoomRepository roomRepository;
    private final InTouchRoomGroupRepository groupRepository;
    private final InTouchRoomGroupLiveKeyRepository groupLiveKeyRepository;
    private final InTouchRoomGroupParticipantRepository groupParticipantRepository;
    @Transactional(readOnly = true)
    public InTouchRoomProgressDto getRoomProgress(Long roomId) {

        InTouchRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        List<InTouchRoomGroup> groups =
                groupRepository.findByRoomIdOrderBySortOrderAsc(roomId);

        List<InTouchRoomGroupProgressDto> groupProgressList = groups.stream()
                .map(group -> calculateGroupProgress(room, group))
                .toList();

        return InTouchRoomProgressDto.builder()
                .roomId(room.getId())
                .buildMode(room.getBuildMode())
                .roomCode(room.getRoomCode())
                .roomStatus(room.getStatus())
                .groups(groupProgressList)
                .roomTitle(room.getTitle())
                .build();
    }

private InTouchRoomGroupProgressDto calculateGroupProgress(
        InTouchRoom room,
        InTouchRoomGroup group
) {
    long totalKeys = groupLiveKeyRepository.countByRoomIdAndGroupId(
            room.getId(),
            group.getId()
    );

    long completedKeys;

    if (room.getBuildMode() == LiveRoomBuildMode.REMOVE_KEYS) {
        completedKeys = groupLiveKeyRepository.countByRoomIdAndGroupIdAndStatus(
                room.getId(),
                group.getId(),
                LiveKeyBuildStatus.REMOVED
        );
    } else {
        completedKeys = groupLiveKeyRepository.countByRoomIdAndGroupIdAndStatus(
                room.getId(),
                group.getId(),
                LiveKeyBuildStatus.PLACED
        );
    }

    double progressPercent = totalKeys == 0
            ? 0
            : (completedKeys * 100.0) / totalKeys;

    LiveKeyBuildStatus targetStatus =
            room.getBuildMode() == LiveRoomBuildMode.REMOVE_KEYS
                    ? LiveKeyBuildStatus.IN_PROGRESS
                    : LiveKeyBuildStatus.NOT_STARTED;

    List<InTouchRoomGroupParticipant> groupParticipants =
            groupParticipantRepository.findByRoomIdAndGroupId(
                    room.getId(),
                    group.getId()
            );

    List<CurrentParticipantKeyDto> currentKeys =
            groupParticipants.stream()
                    .map(gp -> {
                        var participant = gp.getParticipant();

                        var nextKey = groupLiveKeyRepository
                                .findFirstByRoom_IdAndGroup_IdAndAssignedParticipant_IdAndStatusOrderByAssignedOrderAsc(
                                        room.getId(),
                                        group.getId(),
                                        participant.getId(),
                                        targetStatus
                                )
                                .orElse(null);

                        return new CurrentParticipantKeyDto(
                                participant.getId(),
                                participant.getDisplayName(), // adjust if your field name differs
                                participant.getParticipantCode(),
                                nextKey != null ? nextKey.getKeyValue() : null,
                                nextKey != null ? nextKey.getAssignedOrder() : null
                        );
                    })
                    .toList();

    return InTouchRoomGroupProgressDto.builder()
            .groupId(group.getId())
            .groupName(group.getName())
            .totalKeys(totalKeys)
            .completedKeys(completedKeys)
            .progressPercent(progressPercent)
            .completed(totalKeys > 0 && completedKeys == totalKeys)
            .completedAt(group.getCompletedAt())
            .errorCount(group.getErrorCount())
            .score(group.getScore())
            .buildMode(room.getBuildMode())
            .completedPatternCount(group.getCompletedPatternCount())
            .currentParticipantKeys(currentKeys)
            .build();
}
}
