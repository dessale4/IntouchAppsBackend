package com.intouch.IntouchApps.liveroom.dto.response;

import com.intouch.IntouchApps.liveroom.InTouchRoomStatus;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveRoomParticipantAccessResponse {
    private Long roomId;
    private String roomCode;
    private String roomTitle;
    private InTouchRoomStatus roomStatus;
    private List<LiveRoomParticipantAccessRowResponse> participants;
}
