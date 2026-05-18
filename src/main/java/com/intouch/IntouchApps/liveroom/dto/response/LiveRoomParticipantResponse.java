package com.intouch.IntouchApps.liveroom.dto.response;

import com.intouch.IntouchApps.liveroom.ParticipantStatus;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveRoomParticipantResponse {

    private Long id;

    private String participantCode;

    private String displayName;

    private ParticipantStatus status;

    private Boolean activeInRoom;
}