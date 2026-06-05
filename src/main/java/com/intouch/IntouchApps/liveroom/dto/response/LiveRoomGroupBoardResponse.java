package com.intouch.IntouchApps.liveroom.dto.response;

import com.intouch.IntouchApps.liveroom.LiveRoomBuildMode;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveRoomGroupBoardResponse {
    private Long roomId;
    private Long groupId;
    private String groupName;
    private LiveRoomBuildMode buildMode;
    private List<LiveRoomGroupBoardCellResponse> cells;
}
