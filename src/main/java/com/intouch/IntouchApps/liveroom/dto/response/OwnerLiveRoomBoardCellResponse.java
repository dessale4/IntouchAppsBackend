package com.intouch.IntouchApps.liveroom.dto.response;

import com.intouch.IntouchApps.liveroom.LiveKeyBuildStatus;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerLiveRoomBoardCellResponse {
    private Long keyId;
    private Integer columnIndex;
    private String keyValue;
    private String keyFamilyId;
    private LiveKeyBuildStatus status;
    private Long participantId;
    private String participantDisplayName;
}
