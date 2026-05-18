package com.intouch.IntouchApps.liveroom;

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
//            completedKeys = groupLiveKeyRepository.countCorrectlyPlacedKeys(
//                    room.getId(),
//                    group.getId()
//            );
            completedKeys = groupLiveKeyRepository.countByRoomIdAndGroupIdAndStatus(
                    room.getId(),
                    group.getId(),
                    LiveKeyBuildStatus.PLACED
            );
        }

        double progressPercent = totalKeys == 0
                ? 0
                : (completedKeys * 100.0) / totalKeys;

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
                .completedPatternCount(group.getCompletedPatternCount())
                .build();

    }
}
