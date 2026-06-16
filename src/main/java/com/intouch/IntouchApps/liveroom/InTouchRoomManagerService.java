package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.liveroom.dto.response.LiveRoomManagerRoomResponse;
import com.intouch.IntouchApps.liveroom.repository.InTouchRoomGroupBoardRowRepository;
import com.intouch.IntouchApps.liveroom.withPattern.InTouchRoomBoardPatternRepository;
import com.intouch.IntouchApps.liveroom.withPattern.InTouchRoomGroupPatternProgressRepository;
import com.intouch.IntouchApps.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InTouchRoomManagerService {
    private final InTouchRoomRepository roomRepository;
    private final UserRepository userRepository;
    private final InTouchRoomGroupPatternProgressRepository patternProgressRepository;
    private final InTouchRoomGroupBoardRowRepository boardRowRepository;
    private final InTouchRoomGroupLiveKeyRepository groupLiveKeyRepository;
    private final InTouchRoomLiveKeyRepository liveKeyRepository;
    private final InTouchRoomLiveKeyFamilyRepository roomLiveKeyFamilyRepository;
    private final InTouchRoomBoardPatternRepository boardPatternRepository;
    private final InTouchRoomGroupParticipantRepository groupParticipantRepository;
    private final InTouchRoomParticipantRepository participantRepository;
    private final InTouchRoomGroupRepository groupRepository;
    public List<LiveRoomManagerRoomResponse> searchOwnerRooms(String ownerUsername, String roomCode) {
        boolean userExists = userRepository.existsByUserName(ownerUsername.toLowerCase());
        if(!userExists){
            throw new IllegalArgumentException(String.format("An account with userName  %s does not exist.", ownerUsername));
        }
        List<InTouchRoom> userRoomsForManager = roomRepository.findRoomsForManager(ownerUsername, roomCode);
        return userRoomsForManager.stream()
                .map(r->mapToLiveRoomManagerRoomResponse(r)).collect(Collectors.toList());

    }
    private LiveRoomManagerRoomResponse mapToLiveRoomManagerRoomResponse(InTouchRoom room){
        return LiveRoomManagerRoomResponse.builder()
                .roomId(room.getId())
                .roomTitle(room.getTitle())
                .roomCode(room.getRoomCode())
                .status(room.getStatus())
                .buildMode(room.getBuildMode())
                .paidRoom(room.getPaidRoom())
                .lastModifiedDate(room.getUpdatedAt())
                .createdDate(room.getCreatedAt())
                .replayCount(room.getReplayCount())
                .ownerUsername(room.getOwner().getUserName())
                .build();
    }

    @Transactional
    public void updatePaidRoom(Long roomId, boolean paidRoom) {
        InTouchRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found."));

        room.setPaidRoom(paidRoom);
        roomRepository.save(room);
    }
    @Transactional
    public void deleteRoomAsManager(Long roomId) {
        InTouchRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found."));

        // optional: keep this only if you want to prevent deleting active live sessions
//        if (room.getStatus() == InTouchRoomStatus.STARTED ||
//                room.getStatus() == InTouchRoomStatus.PAUSED) {
//            throw new IllegalStateException("Active rooms cannot be deleted.");
//        }

        deleteRoomChildren(roomId);
        log.warn(
                "Live room deleted by manager. roomId={}, roomCode={}, owner={}",
                room.getId(),
                room.getRoomCode(),
                room.getOwner().getUserName()
        );
        roomRepository.delete(room);
    }
    private void deleteRoomChildren(Long roomId) {
        patternProgressRepository.deleteByRoomId(roomId);
        boardRowRepository.deleteByRoomId(roomId);

        groupLiveKeyRepository.deleteByRoomId(roomId);
        liveKeyRepository.deleteByRoomId(roomId);
        roomLiveKeyFamilyRepository.deleteByRoomId(roomId);

        boardPatternRepository.deleteByRoomId(roomId);

        groupParticipantRepository.deleteByRoomId(roomId);

        participantRepository.deleteByRoomId(roomId);
        groupRepository.deleteByRoomId(roomId);
    }
}
