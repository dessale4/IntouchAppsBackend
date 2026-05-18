package com.intouch.IntouchApps.liveroom.dto.response;

import com.intouch.IntouchApps.liveroom.InTouchRoomStatus;
import com.intouch.IntouchApps.liveroom.LiveKeyPlacementStrategy;
import com.intouch.IntouchApps.liveroom.LiveRoomBuildMode;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveRoomResponse {

    private Long id;
    private String roomCode;
    private String title;

    private LiveRoomBuildMode buildMode;
    private LiveKeyPlacementStrategy placementStrategy;

    private Boolean shuffleKeys;
    private Boolean scoringEnabled;

    private InTouchRoomStatus status;
}