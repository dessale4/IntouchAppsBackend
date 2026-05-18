package com.intouch.IntouchApps.liveroom;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LiveRoomParticipantValidator {

    private final InTouchRoomParticipantRepository repository;

    public void ensureUserCanJoin(Integer userId, Long currentRoomId) {
        /*
     A mobile user should not participate in two different active rooms.

     But if the user is rejoining/resuming the SAME active room,
     this should be allowed.
    */
        if (repository.existsActiveParticipantInOtherActiveRoom(userId, currentRoomId)) {
            throw new IllegalStateException(
                    "You are already participating in another active room."
            );
        }
    }
    public void ensureSlotClaimable(
            InTouchRoomParticipant participant,
            Integer currentUserId
    ) {

    /*
     Case 1:
     The participant slot has never been claimed before.

     Requirements:
     - status must still be INVITED
     - no mobile user associated yet

     Result:
     - any authenticated mobile user can claim the slot
    */
        if (participant.getStatus() == ParticipantStatus.INVITED
                && participant.getMobileUser() == null) {
            return;
        }

    /*
     Case 2:
     The participant slot was already claimed by THIS SAME mobile user.

     Requirements:
     - participant already linked to a mobile user
     - linked mobile user matches current authenticated user
     - status is JOINED or ACTIVE

     Result:
     - allow rejoin/resume for same user
     - useful when app restarts or reconnects
    */
        if (participant.getMobileUser() != null
                && participant.getMobileUser().getId().equals(currentUserId)
                && (
                participant.getStatus() == ParticipantStatus.JOINED
                        || participant.getStatus() == ParticipantStatus.ACTIVE
        )) {
            return;
        }

    /*
     All other cases are invalid.

     Examples:
     - different mobile user tries same participant code
     - participant already removed
     - invalid lifecycle state
     - already claimed by another user

     Result:
     - reject the join attempt
    */
        throw new IllegalStateException(
                "Participant code is already claimed or no longer valid."
        );
    }
}
