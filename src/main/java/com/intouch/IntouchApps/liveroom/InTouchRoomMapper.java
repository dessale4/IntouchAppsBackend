package com.intouch.IntouchApps.liveroom;

import com.intouch.IntouchApps.liveroom.dto.response.*;
import org.springframework.stereotype.Component;

@Component
public class InTouchRoomMapper {

    public LiveRoomResponse toRoomResponse(InTouchRoom room) {
        return LiveRoomResponse.builder()
                .id(room.getId())
                .roomCode(room.getRoomCode())
                .title(room.getTitle())
                .buildMode(room.getBuildMode())
                .placementStrategy(room.getPlacementStrategy())
                .shuffleKeys(room.getShuffleKeys())
                .scoringEnabled(room.getScoringEnabled())
                .status(room.getStatus())
                .build();
    }

    public LiveRoomGroupResponse toGroupResponse(InTouchRoomGroup group) {
        return LiveRoomGroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .sortOrder(group.getSortOrder())
                .errorCount(group.getErrorCount())
                .score(group.getScore())
                .completedPatternCount(group.getCompletedPatternCount())
                .build();
    }

    public LiveRoomParticipantResponse toParticipantResponse(
            InTouchRoomParticipant participant
    ) {
        return LiveRoomParticipantResponse.builder()
                .id(participant.getId())
                .participantCode(participant.getParticipantCode())
                .displayName(participant.getDisplayName())
                .status(participant.getStatus())
                .activeInRoom(participant.getActiveInRoom())
                .build();
    }
}