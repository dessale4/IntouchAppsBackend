package com.intouch.IntouchApps.liveroom.dto;

import com.intouch.IntouchApps.liveroom.LiveKeyPlacementStrategy;
import com.intouch.IntouchApps.liveroom.LiveRoomBuildMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomRequest {

    @NotBlank(message = "Room title is required")
    private String title;

    @NotNull(message = "Build mode is required")
    private LiveRoomBuildMode buildMode;

    @Builder.Default
    private LiveKeyPlacementStrategy placementStrategy =
            LiveKeyPlacementStrategy.EXACT_TARGET_POSITION;

    @Builder.Default
    private Boolean shuffleKeys = true;

    @Builder.Default
    private Boolean scoringEnabled = false;

    private String targetStructure;
}