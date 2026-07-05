package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.liveroom.dto.response.MobileJoinRoomResponse;
import com.intouch.IntouchApps.security.SecurityUtils;
import com.intouch.IntouchApps.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InTouchRoomParticipantService {

    private final InTouchRoomParticipantRepository participantRepository;
    private final InTouchRoomRepository roomRepository;
    private final LiveRoomParticipantValidator validator;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final InTouchRoomGroupParticipantRepository groupParticipantRepository;
    private final InTouchRoomProgressPublisher progressPublisher;
    @Transactional
    public MobileJoinRoomResponse joinRoom(String roomCode, String participantCode) {
        Integer currentUserId = securityUtils.getCurrentUserId();

        InTouchRoomParticipant participant =
                participantRepository
                        .findByRoomCodeAndParticipantCode(roomCode, participantCode)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Invalid room code or participant code"
                        ));

        validator.ensureUserCanJoin(currentUserId, participant.getRoom().getId());
        boolean alreadyJoinedThisRoom =
                participantRepository.existsByRoomIdAndMobileUserIdAndClaimedAtIsNotNull(
                        participant.getRoom().getId(),
                        currentUserId
                );

        boolean sameParticipant =
                participant.getMobileUser() != null
                        && participant.getMobileUser().getId().equals(currentUserId);

        if (alreadyJoinedThisRoom && !sameParticipant) {
            throw new IllegalStateException(
                    "You already joined this room with another participant code. Please use your previous participant code."
            );
        }
        validator.ensureSlotClaimable(participant, currentUserId);

        if (participant.getMobileUser() == null) {
            participant.setMobileUser(userRepository.getReferenceById(currentUserId));
            participant.setStatus(ParticipantStatus.JOINED);
            participant.setClaimedAt(Instant.now());
        }
        List<InTouchRoomGroupParticipant> groupAssignments =
                groupParticipantRepository.findByRoomIdAndParticipantId(
                        participant.getRoom().getId(),
                        participant.getId()
                );
        boolean canPlay =
                participant.getRoom().getStatus() == InTouchRoomStatus.STARTED ||
                        participant.getRoom().getStatus() == InTouchRoomStatus.PAUSED;
        InTouchRoomGroupParticipant groupAssignment =
                groupAssignments.isEmpty() ? null : groupAssignments.get(0);
        progressPublisher.publishRoomProgress(participant.getRoom().getId());
        return MobileJoinRoomResponse.builder()
                .roomId(participant.getRoom().getId())
                .buildMode(participant.getRoom().getBuildMode())
                .paidRoom(participant.getRoom().getPaidRoom())
                .roomTitle(participant.getRoom().getTitle())
                .roomCode(participant.getRoom().getRoomCode())
                .roomStatus(participant.getRoom().getStatus())
                .participantId(participant.getId())
                .participantDisplayName(participant.getDisplayName())
                .participantCode(participant.getParticipantCode())
                .participantStatus(participant.getStatus())
                .groupId(groupAssignment != null ? groupAssignment.getGroup().getId() : null)
                .groupName(groupAssignment != null ? groupAssignment.getGroup().getName() : null)
                .canPlay(canPlay)
                .build();
    }
}