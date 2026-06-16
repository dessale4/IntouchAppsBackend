package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.liveroom.dto.response.LiveRoomManagerRoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("manager/live-rooms")
@RequiredArgsConstructor
public class InTouchRoomManagerController {
    private final InTouchRoomManagerService liveRoomManagerService;
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_LIVEROOM_MANAGER')")
    public ResponseEntity<List<LiveRoomManagerRoomResponse>> searchOwnerRooms(
            @RequestParam String ownerUsername,
            @RequestParam(required = false) String roomCode
    ) {
        return ResponseEntity.ok(
                liveRoomManagerService.searchOwnerRooms(ownerUsername, roomCode)
        );
    }
    @PatchMapping("/{roomId}/paid-room")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_LIVEROOM_MANAGER')")
    public ResponseEntity<Void> updatePaidRoom(
            @PathVariable Long roomId,
            @RequestParam boolean paidRoom
    ) {
        liveRoomManagerService.updatePaidRoom(roomId, paidRoom);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/{roomId}/delete")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_LIVEROOM_MANAGER')")
    public ResponseEntity<Void> deleteRoomAsManager(@PathVariable Long roomId) {
        liveRoomManagerService.deleteRoomAsManager(roomId);
        return ResponseEntity.noContent().build();
    }
}
