package com.intouch.IntouchApps.liveroom.dto.response;

import com.intouch.IntouchApps.liveroom.LiveKeyBuildStatus;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveRoomGroupBoardCellResponse {
    private Long groupLiveKeyId;
    private String keyValue;
    private String keyFamilyId;
    private Integer rowIndex;
    private Integer columnIndex;
    private LiveKeyBuildStatus status;
}
