package com.intouch.IntouchApps.liveroom.dto.response;

import com.intouch.IntouchApps.liveroom.LiveKeyBuildStatus;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileBoardCellResponse {

    private Integer columnIndex;
    private String keyValue;
    private LiveKeyBuildStatus status;
}