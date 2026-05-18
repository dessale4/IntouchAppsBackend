package com.intouch.IntouchApps.liveroom.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerLiveRoomGroupDetailResponse {
    private Long roomId;
    private Long groupId;
    private String groupName;

    private Double progressPercent;
    private Integer errorCount;
    private Integer score;
    private Integer completedPatternCount;

    private List<OwnerLiveRoomBoardRowResponse> rows;
    private List<OwnerLiveRoomParticipantSummaryResponse> participants;
}