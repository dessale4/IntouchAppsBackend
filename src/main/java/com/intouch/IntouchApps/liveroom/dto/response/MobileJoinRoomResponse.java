package com.intouch.IntouchApps.liveroom.dto.response;

import com.intouch.IntouchApps.liveroom.InTouchRoomStatus;
import com.intouch.IntouchApps.liveroom.ParticipantStatus;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileJoinRoomResponse {

    private Long roomId;
    private String roomTitle;
    private String roomCode;
    private InTouchRoomStatus roomStatus;

    private Long participantId;
    private String participantDisplayName;
    private String participantCode;
    private ParticipantStatus participantStatus;

    private Long groupId;
    private String groupName;
}