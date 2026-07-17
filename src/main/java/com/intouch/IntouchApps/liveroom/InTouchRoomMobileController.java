package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.liveroom.dto.JoinRoomRequest;
import com.intouch.IntouchApps.liveroom.dto.request.MobilePlaceKeyRequest;
import com.intouch.IntouchApps.liveroom.dto.request.MobileRemoveKeyRequest;
import com.intouch.IntouchApps.liveroom.dto.response.MobileJoinRoomResponse;
import com.intouch.IntouchApps.liveroom.dto.response.MobileMyBoardResponse;
import com.intouch.IntouchApps.liveroom.dto.response.MobileNextKeyResponse;
import com.intouch.IntouchApps.liveroom.dto.response.MobileRoomWorkResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("mobile/live-rooms")
@RequiredArgsConstructor
public class InTouchRoomMobileController {

    private final InTouchRoomParticipantService participantService;
    private final InTouchRoomMobileKeyService mobileKeyService;
    private final InTouchRoomMobileQueryService mobileQueryService;

    @PostMapping("/join")
    public ResponseEntity<MobileJoinRoomResponse> joinRoom(
            @Valid @RequestBody JoinRoomRequest request
    ) {
        return ResponseEntity.ok(
                participantService.joinRoom(
                        request.getRoomCode(),
                        request.getParticipantCode()
                )
        );
    }

    @GetMapping("/current")
    public ResponseEntity<MobileJoinRoomResponse> getCurrentRoom() {
        return participantService.getCurrentRoom()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/current/leave")
    public ResponseEntity<Void> leaveCurrentRoom() {
        participantService.leaveCurrentRoom();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/keys/{groupLiveKeyId}/place")
    public ResponseEntity<MobileNextKeyResponse> placeKey(
            @PathVariable Long groupLiveKeyId,
            @Valid @RequestBody MobilePlaceKeyRequest request
    ) {
        return ResponseEntity.ok(
                mobileKeyService.placeKey(groupLiveKeyId, request)
        );
    }

    @PostMapping("/keys/{groupLiveKeyId}/remove")
    public ResponseEntity<MobileNextKeyResponse> removeKey(
            @PathVariable Long groupLiveKeyId,
            @RequestBody MobileRemoveKeyRequest request
    ) {
        MobileNextKeyResponse response =
                mobileKeyService.removeKey(groupLiveKeyId, request);

        return ResponseEntity.ok(response);
    }
    @GetMapping("/{roomId}/my-board")
    public ResponseEntity<MobileMyBoardResponse> getMyBoard(
            @PathVariable Long roomId
    ) {
        return ResponseEntity.ok(
                mobileQueryService.getMyBoard(roomId)
        );
    }
    @GetMapping("/{roomId}/my-keys")
    public ResponseEntity<MobileRoomWorkResponse> getMyKeys(
            @PathVariable Long roomId
    ) {
        return ResponseEntity.ok(mobileQueryService.getMyRoomWork(roomId));
    }

    @GetMapping("/{roomId}/next-key")
    public ResponseEntity<MobileNextKeyResponse> getNextKey(
            @PathVariable Long roomId
    ) {
        return ResponseEntity.ok(mobileQueryService.getNextKeyForCurrentParticipant(roomId));
    }
}
