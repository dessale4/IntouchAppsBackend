package com.intouch.IntouchApps.liveroom.dto.response;

import com.intouch.IntouchApps.liveroom.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileRoomWorkResponse {

    private Long roomId;
    private String roomCode;
    private String title;

    private LiveRoomBuildMode buildMode;
    private LiveKeyPlacementStrategy placementStrategy;
    private InTouchRoomStatus status;

    private Long groupId;
    private String groupName;

    private String targetStructure; // optional JSON guidance

    private List<MobileLiveKeyResponse> myKeys;
}