package com.intouch.IntouchApps.liveroom.dto.response;

import com.intouch.IntouchApps.liveroom.InTouchRoomStatus;
import com.intouch.IntouchApps.liveroom.LiveRoomBuildMode;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveRoomManagerRoomResponse {
    private Long roomId;
    private String roomTitle;
    private String roomCode;
    private InTouchRoomStatus status;
    private LiveRoomBuildMode buildMode;
    private Boolean paidRoom;
    private Integer replayCount;
    private String ownerUsername;
    private Instant createdDate;
    private Instant lastModifiedDate;
}
