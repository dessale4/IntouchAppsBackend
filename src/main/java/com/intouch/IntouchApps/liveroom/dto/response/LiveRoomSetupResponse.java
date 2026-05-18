package com.intouch.IntouchApps.liveroom.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveRoomSetupResponse {

    private LiveRoomResponse room;
    private List<LiveRoomGroupResponse> groups;
    private List<LiveRoomParticipantResponse> participants;
    private List<LiveRoomAssignmentResponse> assignments;
}