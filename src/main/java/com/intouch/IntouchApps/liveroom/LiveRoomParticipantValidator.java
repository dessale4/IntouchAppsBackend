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

    public void ensureRoomAllowsJoin(
            InTouchRoomParticipant participant,
            Integer currentUserId
    ) {
        InTouchRoom room = participant.getRoom();
        boolean sameUser = participant.getMobileUser() != null
                && participant.getMobileUser().getId().equals(currentUserId);

        // Keep LEFT handling in ensureSlotClaimable so the user receives the
        // existing owner-reactivation guidance in every room lifecycle state.
        if (sameUser && participant.getStatus() == ParticipantStatus.LEFT) {
            return;
        }

        if (Boolean.TRUE.equals(room.getDeleted()) ||
                room.getStatus() == InTouchRoomStatus.DELETED) {
            throw new IllegalStateException("Deleted room cannot be joined.");
        }

        if (room.getStatus() == InTouchRoomStatus.COMPLETED ||
                room.getStatus() == InTouchRoomStatus.CANCELLED) {
            throw new IllegalStateException("Completed or cancelled room cannot be joined.");
        }

        if (participant.getStatus() == ParticipantStatus.INVITED &&
                participant.getMobileUser() == null) {
            if (room.getStatus() != InTouchRoomStatus.DRAFT &&
                    room.getStatus() != InTouchRoomStatus.READY) {
                throw new IllegalStateException(
                        "New participants can join only DRAFT or READY rooms."
                );
            }
            return;
        }

        if (sameUser && participant.getStatus() == ParticipantStatus.JOINED) {
            if (room.getStatus() != InTouchRoomStatus.DRAFT &&
                    room.getStatus() != InTouchRoomStatus.READY) {
                throw new IllegalStateException(
                        "Joined participants can re-enter only DRAFT or READY rooms."
                );
            }
            return;
        }

        if (sameUser && participant.getStatus() == ParticipantStatus.ACTIVE) {
            if (room.getStatus() != InTouchRoomStatus.STARTED &&
                    room.getStatus() != InTouchRoomStatus.PAUSED) {
                throw new IllegalStateException(
                        "Active participants can re-enter only STARTED or PAUSED rooms."
                );
            }
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

        boolean sameUser =
                participant.getMobileUser() != null
                        && participant.getMobileUser().getId().equals(currentUserId);

        if (sameUser && participant.getStatus() == ParticipantStatus.LEFT) {
            throw new IllegalStateException(
                    "You already left this room. Ask the room owner to reactivate your participation if you want to rejoin."
            );
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
        if (sameUser
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
