package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.liveroom.dto.response.MobileNextKeyResponse;
import com.intouch.IntouchApps.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InTouchRoomPooledKeyClaimService {

    private final InTouchRoomRepository roomRepository;
    private final InTouchRoomParticipantRepository participantRepository;
    private final InTouchRoomGroupParticipantRepository groupParticipantRepository;
    private final InTouchRoomGroupLiveKeyRepository groupLiveKeyRepository;
    private final InTouchRoomLifecycleValidator lifecycleValidator;
    private final InTouchRoomMobileQueryService mobileQueryService;
    private final InTouchRoomProgressPublisher progressPublisher;
    private final SecurityUtils securityUtils;

    @Transactional
    public MobileNextKeyResponse claimNextKey(Long roomId) {
        return resolveNextKey(roomId, false);
    }

    @Transactional
    public MobileNextKeyResponse nextKeyAfterSuccessfulWork(Long roomId) {
        return resolveNextKey(roomId, true);
    }

    private MobileNextKeyResponse resolveNextKey(
            Long roomId,
            boolean allowCompletedNoMoreResponse
    ) {
        InTouchRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found."));
        if (Boolean.TRUE.equals(room.getDeleted())) {
            throw new IllegalStateException("Deleted room cannot provide keys.");
        }
        boolean completedAfterWork = allowCompletedNoMoreResponse &&
                room.getStatus() == InTouchRoomStatus.COMPLETED;
        if (!completedAfterWork) {
            lifecycleValidator.ensureGameplayAllowed(room);
        }

        Integer currentUserId = securityUtils.getCurrentUserId();
        InTouchRoomParticipant participant = participantRepository
                .findByRoomIdAndMobileUserIdForUpdate(roomId, currentUserId)
                .orElseThrow(() -> new IllegalStateException(
                        "You are not assigned to this live room."
                ));

        if (participant.getStatus() != ParticipantStatus.ACTIVE ||
                !Boolean.TRUE.equals(participant.getActiveInRoom())) {
            throw new IllegalStateException("Participant is not active in this room.");
        }

        if (completedAfterWork) {
            return mobileQueryService.buildNextKeyResponse(room, currentUserId);
        }

        List<InTouchRoomGroupParticipant> assignments =
                groupParticipantRepository.findByRoomIdAndParticipantId(
                        roomId,
                        participant.getId()
                );
        if (assignments.isEmpty()) {
            throw new IllegalStateException(
                    "Participant must be assigned to a group before claiming keys."
            );
        }

        Long groupId = assignments.get(0).getGroup().getId();
        LiveKeyBuildStatus unfinishedStatus =
                room.getBuildMode() == LiveRoomBuildMode.REMOVE_KEYS
                        ? LiveKeyBuildStatus.IN_PROGRESS
                        : LiveKeyBuildStatus.NOT_STARTED;

        Optional<InTouchRoomGroupLiveKey> ownKey = groupLiveKeyRepository
                .findFirstByRoom_IdAndGroup_IdAndAssignedParticipant_IdAndAssignmentStateAndStatusOrderByAssignedOrderAscIdAsc(
                        roomId,
                        groupId,
                        participant.getId(),
                        LiveKeyAssignmentState.ASSIGNED,
                        unfinishedStatus
                );
        if (ownKey.isPresent()) {
            return mobileQueryService.buildNextKeyResponse(room, currentUserId);
        }

        Optional<InTouchRoomGroupLiveKey> pooledKey = groupLiveKeyRepository
                .findNextPooledKeyForUpdate(
                        roomId,
                        groupId,
                        unfinishedStatus.name()
                );
        if (pooledKey.isEmpty()) {
            return mobileQueryService.buildNextKeyResponse(room, currentUserId);
        }

        InTouchRoomGroupLiveKey claimedKey = pooledKey.get();
        claimedKey.setAssignmentState(LiveKeyAssignmentState.ASSIGNED);
        claimedKey.setAssignedParticipant(participant);
        groupLiveKeyRepository.saveAndFlush(claimedKey);

        progressPublisher.publishRoomProgress(roomId);
        return mobileQueryService.buildNextKeyResponse(room, currentUserId);
    }
}
