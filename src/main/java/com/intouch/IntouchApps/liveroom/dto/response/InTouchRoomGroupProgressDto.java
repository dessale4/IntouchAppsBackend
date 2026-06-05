package com.intouch.IntouchApps.liveroom.dto.response;
import com.intouch.IntouchApps.liveroom.LiveRoomBuildMode;
import lombok.*;

import java.time.Instant;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InTouchRoomGroupProgressDto {

    private Long groupId;
    private String groupName;

    private long totalKeys;
    private long completedKeys;
    private double progressPercent;

    private boolean completed;
    private Integer errorCount;
    private Integer score;
    private Integer completedPatternCount;
    private Instant completedAt;
    private String roomCode;
    private String roomTitle;
    private LiveRoomBuildMode buildMode;

}
