package com.intouch.IntouchApps.liveroom.dto.response;

import com.intouch.IntouchApps.liveroom.InTouchRoomStatus;
import com.intouch.IntouchApps.liveroom.LiveRoomBuildMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileCompletedRoomReviewResponse {

    private Long roomId;
    private String roomTitle;
    private InTouchRoomStatus roomStatus;
    private LiveRoomBuildMode buildMode;
    private String participantDisplayName;
    private String groupName;
    private MobileMyBoardResponse board;
    private InTouchRoomGroupProgressDto progress;
}
