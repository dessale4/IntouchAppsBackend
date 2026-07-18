package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.liveroom.dto.response.InTouchRoomGroupProgressDto;
import com.intouch.IntouchApps.liveroom.dto.response.InTouchRoomProgressDto;
import com.intouch.IntouchApps.liveroom.dto.response.MobileCompletedRoomReviewResponse;
import com.intouch.IntouchApps.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InTouchRoomCompletedReviewService {

    private final InTouchRoomRepository roomRepository;
    private final InTouchRoomParticipantRepository participantRepository;
    private final InTouchRoomGroupParticipantRepository groupParticipantRepository;
    private final InTouchRoomMobileQueryService mobileQueryService;
    private final InTouchRoomProgressService progressService;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public MobileCompletedRoomReviewResponse getCompletedReview(String roomCode) {
        InTouchRoom room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new IllegalArgumentException("Room not found."));

        if (Boolean.TRUE.equals(room.getDeleted()) ||
                room.getStatus() == InTouchRoomStatus.DELETED) {
            throw new IllegalStateException("Deleted room cannot be reviewed.");
        }
        if (room.getStatus() != InTouchRoomStatus.COMPLETED) {
            throw new IllegalStateException("Only completed rooms can be reviewed.");
        }

        Integer currentUserId = securityUtils.getCurrentUserId();
        InTouchRoomParticipant participant = participantRepository
                .findByRoomIdAndMobileUserId(room.getId(), currentUserId)
                .orElseThrow(() -> new IllegalStateException(
                        "You are not assigned to this live room."
                ));

        List<InTouchRoomGroupParticipant> assignments = groupParticipantRepository
                .findByRoomIdAndParticipantId(room.getId(), participant.getId());
        if (assignments.isEmpty()) {
            throw new IllegalStateException(
                    "Participant is not assigned to a group in this room."
            );
        }

        InTouchRoomGroup group = assignments.get(0).getGroup();
        InTouchRoomProgressDto roomProgress = progressService.getRoomProgress(room.getId());
        InTouchRoomGroupProgressDto groupProgress = roomProgress.getGroups().stream()
                .filter(progress -> progress.getGroupId().equals(group.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Group progress was not found for this room."
                ));

        return MobileCompletedRoomReviewResponse.builder()
                .roomId(room.getId())
                .roomTitle(room.getTitle())
                .roomStatus(room.getStatus())
                .buildMode(room.getBuildMode())
                .participantDisplayName(participant.getDisplayName())
                .groupName(group.getName())
                .board(mobileQueryService.getCompletedReviewBoard(room))
                .progress(groupProgress)
                .build();
    }
}
