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
        if (room.getStatus() == InTouchRoomStatus.STARTED) {
            throw new IllegalStateException(
                    "Started room should be cancelled before deletion."
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
}