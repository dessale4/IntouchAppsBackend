package com.intouch.IntouchApps.liveroom.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveRoomGroupResponse {

    private Long id;
    private String name;
    private Integer sortOrder;

    private Integer errorCount;
    private Integer score;
    private Integer completedPatternCount;
}