package com.intouch.IntouchApps.liveroom;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class InTouchRoomStartValidator {

    private final InTouchRoomGroupRepository groupRepository;
    private final InTouchRoomParticipantRepository participantRepository;
    private final InTouchRoomGroupParticipantRepository groupParticipantRepository;
    private final InTouchRoomLiveKeyRepository liveKeyRepository;
    private final InTouchRoomGroupLiveKeyRepository groupLiveKeyRepository;

    public void validateCanStart(InTouchRoom room) {
        if (room.getDeleted()) {
            throw new IllegalStateException("Deleted room cannot be started.");
        }
        if (room.getStatus() != InTouchRoomStatus.DRAFT &&
                room.getStatus() != InTouchRoomStatus.READY) {
            throw new IllegalStateException("Only DRAFT or READY rooms can be started.");
        }

        Long roomId = room.getId();
        List<InTouchRoomParticipant> unclaimedParticipants =
                participantRepository.findUnclaimedParticipantsByRoomId(roomId);

        long groupCount = groupRepository.countByRoomId(roomId);
        if (groupCount == 0) {
            throw new IllegalStateException("Room must have at least one group.");
        }

        long participantCount = participantRepository.countByRoomIdAndStatus(
                roomId,
                ParticipantStatus.JOINED
        );
        if (participantCount == 0) {
            throw new IllegalStateException("Room must have at least one participant.");
        }

        long activeKeyCount = liveKeyRepository.countByRoomIdAndActiveTrue(roomId);
        if (activeKeyCount == 0) {
            throw new IllegalStateException("Room must have at least one active live key.");
        }

        long activeParticipantCount =
                participantRepository.countByRoomIdAndStatus(
                        roomId,
                        ParticipantStatus.JOINED
                );

        long assignedParticipantCount =
                groupParticipantRepository.countDistinctAssignedActiveParticipants(roomId);

        if (activeParticipantCount != assignedParticipantCount) {
            throw new IllegalStateException(
                    "Every participant must be assigned to exactly one group before starting."
            );
        }
        if (!unclaimedParticipants.isEmpty()) {
            String codes = unclaimedParticipants.stream()
                    .map(p -> p.getDisplayName() + " (" + p.getParticipantCode() + ")")
                    .collect(Collectors.joining(", "));

            throw new IllegalStateException(
                    "Room cannot start. These participants have not joined yet: " + codes
            );
        }
        long existingGroupKeyCount = groupLiveKeyRepository.countByRoomId(roomId);
        if (existingGroupKeyCount > 0) {
            throw new IllegalStateException("Room has already generated group live keys.");
        }

        if (activeKeyCount == 0) {
            throw new IllegalStateException(
                    "Room cannot start. Please add at least one active key."
            );
        }

        List<InTouchRoomGroup> groups =
                groupRepository.findByRoomIdOrderBySortOrderAsc(room.getId());

        for (InTouchRoomGroup group : groups) {
            long groupParticipantCount =
                    groupParticipantRepository.countByRoomIdAndGroupIdAndParticipantStatus(
                            room.getId(),
                            group.getId(),
                            ParticipantStatus.JOINED
                    );

            if (groupParticipantCount > activeKeyCount) {
                throw new IllegalStateException(
                        "Room cannot start. Group " + group.getName()
                                + " has " + groupParticipantCount
                                + " participants but only " + activeKeyCount
                                + " active keys."
                );
            }
        }
    }
}
