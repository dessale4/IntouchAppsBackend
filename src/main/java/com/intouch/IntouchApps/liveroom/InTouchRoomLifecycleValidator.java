package com.intouch.IntouchApps.liveroom;

import org.springframework.stereotype.Component;

@Component
public class InTouchRoomLifecycleValidator {

    public void ensureEditableBeforeStart(InTouchRoom room) {
        if (room.getDeleted()) {
            throw new IllegalStateException("Deleted room cannot be edited.");
        }

        if (room.getStatus() != InTouchRoomStatus.DRAFT &&
                room.getStatus() != InTouchRoomStatus.READY) {
            throw new IllegalStateException(
                    "Room can only be edited before it starts."
            );
        }
    }
    public void ensureCanReset(InTouchRoom room) {
        if (room.getStatus() != InTouchRoomStatus.COMPLETED &&
                room.getStatus() != InTouchRoomStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Only completed or cancelled rooms can be reset."
            );
        }
    }
    public void ensureCanStart(InTouchRoom room) {
        if (room.getDeleted()) {
            throw new IllegalStateException("Deleted room cannot be started.");
        }

        if (room.getStatus() != InTouchRoomStatus.DRAFT &&
                room.getStatus() != InTouchRoomStatus.READY) {
            throw new IllegalStateException(
                    "Only DRAFT or READY rooms can be started."
            );
        }
    }

    public void ensureParticipantCanUpdate(InTouchRoom room) {
        if (room.getDeleted()) {
            throw new IllegalStateException("Deleted room cannot be updated.");
        }

        if (room.getStatus() != InTouchRoomStatus.STARTED) {
            throw new IllegalStateException(
                    "Participants can update only STARTED rooms."
            );
        }
    }

    public void ensureCanDelete(InTouchRoom room) {
        if (room.getStatus() == InTouchRoomStatus.STARTED || room.getStatus() == InTouchRoomStatus.PAUSED) {
            throw new IllegalStateException(
                    room.getStatus().toString() +" room should be cancelled before deletion."
            );
        }
    }

    public void ensureCanCancel(InTouchRoom room) {
        if (room.getDeleted()) {
            throw new IllegalStateException("Deleted room cannot be cancelled.");
        }

        if (room.getStatus() == InTouchRoomStatus.COMPLETED ||
                room.getStatus() == InTouchRoomStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Completed or cancelled room cannot be cancelled again."
            );
        }
    }

    public void ensureCanPause(InTouchRoom room) {
        if (room.getStatus() != InTouchRoomStatus.STARTED) {
            throw new IllegalStateException("Only started rooms can be paused.");
        }
    }

    public void ensureCanResume(InTouchRoom room) {
        if (room.getStatus() != InTouchRoomStatus.PAUSED) {
            throw new IllegalStateException("Only paused rooms can be resumed.");
        }
    }

    public void ensureGameplayAllowed(InTouchRoom room) {
        if (Boolean.TRUE.equals(room.getDeleted()) ||
                room.getStatus() == InTouchRoomStatus.DELETED) {
            throw new IllegalStateException("Deleted room cannot be used for gameplay.");
        }

        if (room.getStatus() == InTouchRoomStatus.PAUSED) {
            throw new IllegalStateException("Room paused. Please wait for the room owner to resume it.");
        }

        if (room.getStatus() == InTouchRoomStatus.CANCELLED) {
            throw new IllegalStateException("Room has been cancelled by the owner.");
        }

        if (room.getStatus() != InTouchRoomStatus.STARTED) {
            throw new IllegalStateException("Room is not started yet.");
        }
    }
    public void ensureCanReleaseParticipantClaim(
            InTouchRoom room,
            InTouchRoomParticipant participant
    ) {
        if (Boolean.TRUE.equals(room.getDeleted()) ||
                room.getStatus() == InTouchRoomStatus.DELETED) {
            throw new IllegalStateException("Deleted room cannot release participant claims.");
        }

        if (room.getStatus() != InTouchRoomStatus.DRAFT &&
                room.getStatus() != InTouchRoomStatus.READY) {
            throw new IllegalStateException(
                    "Participant claims can be released only in DRAFT or READY rooms."
            );
        }

        if (participant.getMobileUser() == null) {
            throw new IllegalStateException(
                    "Participant is not currently claimed."
            );
        }
    }
}
