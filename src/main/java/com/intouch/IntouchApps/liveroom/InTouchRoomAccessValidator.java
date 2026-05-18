package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InTouchRoomAccessValidator {

    private final SecurityUtils securityUtils;

    public void ensureRoomOwner(InTouchRoom room) {
        Integer currentUserId = securityUtils.getCurrentUserId();

        if (room.getOwner() == null ||
                room.getOwner().getId() == null ||
                !room.getOwner().getId().equals(currentUserId)) {
            throw new IllegalStateException("You are not the owner of this room.");
        }
    }

    public void ensureRoomOwnerOrAdmin(InTouchRoom room) {
        // Start simple with owner check.
        // Later you can add ROLE_ADMIN / ROLE_MANAGER support here.
        ensureRoomOwner(room);
    }
}