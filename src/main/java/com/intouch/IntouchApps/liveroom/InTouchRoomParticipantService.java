package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.liveroom.dto.response.MobileJoinRoomResponse;
import com.intouch.IntouchApps.security.SecurityUtils;
import com.intouch.IntouchApps.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InTouchRoomParticipantService {

    private static final String MULTIPLE_CURRENT_ROOMS_MESSAGE =
            "Multiple current live-room participations found. Please contact support.";

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
        validator.ensureRoomAllowsJoin(participant, currentUserId);
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
        MobileJoinRoomResponse response = toMobileJoinRoomResponse(participant);
        progressPublisher.publishRoomProgress(participant.getRoom().getId());
        return response;
    }

    @Transactional(readOnly = true)
    public Optional<MobileJoinRoomResponse> getCurrentRoom() {
        Integer currentUserId = securityUtils.getCurrentUserId();

        return resolveCurrentParticipant(currentUserId)
                .map(this::toMobileJoinRoomResponse);
    }

    @Transactional
    public void leaveCurrentRoom() {
        Integer currentUserId = securityUtils.getCurrentUserId();
        InTouchRoomParticipant participant =
                resolveCurrentParticipant(currentUserId)
                        .orElseThrow(() -> new IllegalStateException(
                                "No current live room participation found."
                        ));

        participant.setStatus(ParticipantStatus.LEFT);
        participant.setActiveInRoom(false);
        participantRepository.save(participant);
        progressPublisher.publishRoomProgress(participant.getRoom().getId());
    }

    private Optional<InTouchRoomParticipant> resolveCurrentParticipant(Integer userId) {
        List<InTouchRoomParticipant> matches =
                participantRepository.findCurrentResumableParticipants(userId);

        if (matches.size() > 1) {
            log.error(
                    "Multiple current live-room participations for userId={}: {}",
                    userId,
                    matches.stream()
                            .map(participant -> "participantId=" + participant.getId()
                                    + ", roomId=" + participant.getRoom().getId())
                            .toList()
            );
            throw new IllegalStateException(MULTIPLE_CURRENT_ROOMS_MESSAGE);
        }

        return matches.stream().findFirst();
    }

    private MobileJoinRoomResponse toMobileJoinRoomResponse(
            InTouchRoomParticipant participant
    ) {
        List<InTouchRoomGroupParticipant> groupAssignments =
                groupParticipantRepository.findByRoomIdAndParticipantId(
                        participant.getRoom().getId(),
                        participant.getId()
                );
        boolean canPlay =
                participant.getRoom().getStatus() == InTouchRoomStatus.STARTED;
        InTouchRoomGroupParticipant groupAssignment =
                groupAssignments.isEmpty() ? null : groupAssignments.get(0);
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
