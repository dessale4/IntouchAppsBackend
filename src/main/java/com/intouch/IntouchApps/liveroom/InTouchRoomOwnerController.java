package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.liveroom.dto.*;
import com.intouch.IntouchApps.liveroom.dto.response.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("live-rooms")
@RequiredArgsConstructor
public class InTouchRoomOwnerController {

    private final InTouchRoomOwnerCommandService ownerCommandService;
    private final InTouchRoomStartService startService;
    private final InTouchRoomMapper mapper;
    private final InTouchRoomProgressService progressService;
    private  final InTouchRoomOwnerQueryService ownerQueryService;
    @PostMapping
    public ResponseEntity<LiveRoomResponse> createRoom(
            @Valid @RequestBody CreateRoomRequest request
    ) {
        InTouchRoom room = ownerCommandService.createRoom(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toRoomResponse(room));
    }
    @GetMapping
    public ResponseEntity<List<LiveRoomResponse>> getMyRooms() {
        return ResponseEntity.ok(ownerCommandService.getMyRooms());
    }
    @PostMapping("/{roomId}/groups")
    public ResponseEntity<LiveRoomGroupResponse> addGroup(
            @PathVariable Long roomId,
            @Valid @RequestBody CreateGroupRequest request
    ) {
        InTouchRoomGroup group =
                ownerCommandService.addGroup(roomId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toGroupResponse(group));
    }
    @GetMapping("/{roomId}/participants/{participantId}/details")
    public ResponseEntity<OwnerLiveRoomParticipantDetailResponse> getParticipantDetail(
            @PathVariable Long roomId,
            @PathVariable Long participantId
    ) {
        return ResponseEntity.ok(
                ownerQueryService.getParticipantDetail(roomId, participantId)
        );
    }
    @PostMapping("/{roomId}/groups/bulk")
    public ResponseEntity<List<LiveRoomGroupResponse>> addGroupsBulk(
            @PathVariable Long roomId,
            @Valid @RequestBody BulkCreateGroupsRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ownerCommandService.addGroupsBulk(roomId, request));
    }
    @PostMapping("/{roomId}/participants/bulk")
    public ResponseEntity<List<LiveRoomParticipantResponse>> addParticipantSlotsBulk(
            @PathVariable Long roomId,
            @Valid @RequestBody BulkCreateParticipantSlotsRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ownerCommandService.addParticipantSlotsBulk(roomId, request));
    }
    @PostMapping("/{roomId}/participants")
    public ResponseEntity<LiveRoomParticipantResponse> addParticipantSlot(
            @PathVariable Long roomId,
            @Valid @RequestBody AddParticipantSlotRequest request
    ) {
        InTouchRoomParticipant participant =
                ownerCommandService.addParticipantSlot(roomId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toParticipantResponse(participant));
    }

    @PostMapping("/{roomId}/group-assignments")
    public ResponseEntity<Void> assignParticipantToGroup(
            @PathVariable Long roomId,
            @Valid @RequestBody AssignParticipantToGroupRequest request
    ) {
        ownerCommandService.assignParticipantToGroup(roomId, request);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{roomId}/reset")
    public ResponseEntity<Void> resetCompletedRoom(@PathVariable Long roomId) {
        ownerCommandService.resetRoom(roomId);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{roomId}/template")
    public ResponseEntity<Void> createTemplate(
            @PathVariable Long roomId,
            @Valid @RequestBody CreateLiveRoomTemplateRequest request
    ) {
        ownerCommandService.createTemplate(roomId, request);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{roomId}/groups/{groupId}/details")
    public ResponseEntity<OwnerLiveRoomGroupDetailResponse> getGroupDetail(
            @PathVariable Long roomId,
            @PathVariable Long groupId
    ) {
        return ResponseEntity.ok(
                ownerQueryService.getGroupDetail(roomId, groupId)
        );
    }
    @GetMapping("/{roomId}/setup")
    public ResponseEntity<LiveRoomSetupResponse> getRoomSetup(
            @PathVariable Long roomId
    ) {
        return ResponseEntity.ok(ownerCommandService.getRoomSetup(roomId));
    }
    @DeleteMapping("/{roomId}/groups/{groupId}")
    public ResponseEntity<Void> deleteGroup(
            @PathVariable Long roomId,
            @PathVariable Long groupId
    ) {
        ownerCommandService.deleteGroup(roomId, groupId);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/{roomId}/participants/{participantId}")
    public ResponseEntity<Void> removeParticipantSlot(
            @PathVariable Long roomId,
            @PathVariable Long participantId
    ) {
        ownerCommandService.removeParticipantSlot(roomId, participantId);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{roomId}/start")
    public ResponseEntity<Void> startRoom(@PathVariable Long roomId) {
        startService.startRoom(roomId);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{roomId}/pause")
    public ResponseEntity<Void> pauseRoom(@PathVariable Long roomId) {
        ownerCommandService.pauseRoom(roomId);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{roomId}/resume")
    public ResponseEntity<Void> resumeRoom(@PathVariable Long roomId) {
        ownerCommandService.resumeRoom(roomId);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{roomId}/cancel")
    public ResponseEntity<Void> cancelRoom(@PathVariable Long roomId) {
        ownerCommandService.cancelRoom(roomId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        ownerCommandService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{roomId}/progress")
    public ResponseEntity<InTouchRoomProgressDto> getRoomProgress(
            @PathVariable Long roomId
    ) {
        return ResponseEntity.ok(progressService.getRoomProgress(roomId));
    }
}