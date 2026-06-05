package com.intouch.IntouchApps.liveroom.dto.response;

import com.intouch.IntouchApps.liveroom.InTouchRoomStatus;
import com.intouch.IntouchApps.liveroom.LiveRoomBuildMode;
import com.intouch.IntouchApps.liveroom.dto.response.InTouchRoomGroupProgressDto;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InTouchRoomProgressDto {

    private Long roomId;
    private String roomCode;
    private InTouchRoomStatus roomStatus;

    private List<InTouchRoomGroupProgressDto> groups;
    private String roomTitle;
    private LiveRoomBuildMode buildMode;
}