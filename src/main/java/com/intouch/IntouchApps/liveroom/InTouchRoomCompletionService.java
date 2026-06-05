package com.intouch.IntouchApps.liveroom;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InTouchRoomCompletionService {

    private final InTouchRoomRepository roomRepository;
    private final InTouchRoomGroupRepository groupRepository;
    private final InTouchRoomGroupLiveKeyRepository groupLiveKeyRepository;

    @Transactional
    public void updateCompletionStatus(Long roomId) {
        InTouchRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        List<InTouchRoomGroup> groups =
                groupRepository.findByRoomIdOrderBySortOrderAsc(roomId);

        boolean allGroupsCompleted = true;
        Instant now = Instant.now();

        for (InTouchRoomGroup group : groups) {
            long totalKeys = groupLiveKeyRepository.countByRoomIdAndGroupId(
                    roomId,
                    group.getId()
            );

            long completedKeys;

            if (room.getBuildMode() == LiveRoomBuildMode.REMOVE_KEYS) {
                completedKeys = groupLiveKeyRepository.countByRoomIdAndGroupIdAndStatus(
                        roomId,
                        group.getId(),
                        LiveKeyBuildStatus.REMOVED
                );
            } else {
//                completedKeys = groupLiveKeyRepository.countCorrectlyPlacedKeys(
//                        roomId,
//                        group.getId()
//                );
                completedKeys = groupLiveKeyRepository.countByRoomIdAndGroupIdAndStatus(
                        room.getId(),
                        group.getId(),
                        LiveKeyBuildStatus.PLACED
                );
            }

            boolean groupCompleted = totalKeys > 0 && completedKeys == totalKeys;

            if (groupCompleted && group.getCompletedAt() == null) {
                group.setCompletedAt(now);
            }

            if (!groupCompleted) {
                group.setCompletedAt(null);
                allGroupsCompleted = false;
            }
        }

        if (allGroupsCompleted && room.getStatus() == InTouchRoomStatus.STARTED) {
            room.setStatus(InTouchRoomStatus.COMPLETED);
        }

        groupRepository.saveAll(groups);
        roomRepository.save(room);
    }
}