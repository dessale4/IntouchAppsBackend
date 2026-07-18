package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.liveroom.dto.response.ReleaseUnfinishedKeysResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InTouchRoomKeyPoolService {

    private final InTouchRoomRepository roomRepository;
    private final InTouchRoomParticipantRepository participantRepository;
    private final InTouchRoomGroupParticipantRepository groupParticipantRepository;
    private final InTouchRoomGroupLiveKeyRepository groupLiveKeyRepository;
    private final InTouchRoomAccessValidator accessValidator;
    private final InTouchRoomProgressPublisher progressPublisher;

    @Transactional
    public ReleaseUnfinishedKeysResponse releaseUnfinishedKeys(
            Long roomId,
            Long participantId
    ) {
        InTouchRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found."));
        accessValidator.ensureRoomOwner(room);

        if (Boolean.TRUE.equals(room.getDeleted()) ||
                (room.getStatus() != InTouchRoomStatus.STARTED &&
                        room.getStatus() != InTouchRoomStatus.PAUSED)) {
            throw new IllegalStateException(
                    "Unfinished keys can only be released from a started or paused room."
            );
        }

        InTouchRoomParticipant participant = participantRepository
                .findByIdAndRoomIdForUpdate(participantId, roomId)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found."));

        if (participant.getStatus() != ParticipantStatus.LEFT) {
            throw new IllegalStateException(
                    "Only a participant who left may have unfinished keys released."
            );
        }

        List<InTouchRoomGroupParticipant> assignments =
                groupParticipantRepository.findByRoomIdAndParticipantId(
                        roomId,
                        participantId
                );
        if (assignments.isEmpty()) {
            throw new IllegalStateException(
                    "Participant must be assigned to a group before keys can be released."
            );
        }

        Long groupId = assignments.get(0).getGroup().getId();
        LiveKeyBuildStatus unfinishedStatus =
                room.getBuildMode() == LiveRoomBuildMode.REMOVE_KEYS
                        ? LiveKeyBuildStatus.IN_PROGRESS
                        : LiveKeyBuildStatus.NOT_STARTED;

        List<InTouchRoomGroupLiveKey> keys = groupLiveKeyRepository
                .findReleasableKeysForUpdate(
                        roomId,
                        groupId,
                        participantId,
                        LiveKeyAssignmentState.ASSIGNED,
                        unfinishedStatus
                );
        if (keys.isEmpty()) {
            throw new IllegalStateException(
                    "This participant has no unfinished keys available to release."
            );
        }

        Instant pooledAt = Instant.now();
        for (InTouchRoomGroupLiveKey key : keys) {
            key.setAssignmentState(LiveKeyAssignmentState.POOLED);
            key.setAssignedParticipant(null);
            key.setReleasedFromParticipant(participant);
            key.setPooledAt(pooledAt);
        }
        groupLiveKeyRepository.saveAll(keys);

        progressPublisher.publishRoomProgress(roomId);
        return new ReleaseUnfinishedKeysResponse(keys.size());
    }
}
