package com.intouch.IntouchApps.liveroom.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveRoomAssignmentResponse {

    private Long id;
    private Long participantId;
    private String participantDisplayName;
    private Long groupId;
    private String groupName;
}