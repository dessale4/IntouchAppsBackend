package com.intouch.IntouchApps.liveroom.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileMyBoardResponse {

    private Long roomId;
    private Long groupId;
    private String groupName;
    private Long participantId;
    private String participantDisplayName;
    private List<MobileBoardRowResponse> rows;
}