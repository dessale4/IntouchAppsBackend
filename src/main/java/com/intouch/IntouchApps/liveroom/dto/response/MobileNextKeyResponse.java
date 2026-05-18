package com.intouch.IntouchApps.liveroom.dto.response;

import com.intouch.IntouchApps.liveroom.InTouchRoomStatus;
import com.intouch.IntouchApps.liveroom.LiveRoomBuildMode;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileNextKeyResponse {

    private Long roomId;
    private Long groupId;
    private String groupName;
    private String roomTitle;
    private InTouchRoomStatus roomStatus;
    private LiveRoomBuildMode buildMode;
    private MobileLiveKeyResponse nextKey;

    private boolean noMoreKeys;

    private long totalAssignedKeys;
    private long completedAssignedKeys;
    private long remainingAssignedKeys;
    private String keyFamilyId;
}