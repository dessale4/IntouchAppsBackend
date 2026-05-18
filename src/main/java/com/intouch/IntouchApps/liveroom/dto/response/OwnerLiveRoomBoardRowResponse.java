package com.intouch.IntouchApps.liveroom.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerLiveRoomBoardRowResponse {
    private Integer rowIndex;
    private String keyFamilyId;
    private List<OwnerLiveRoomBoardCellResponse> cells;
}
