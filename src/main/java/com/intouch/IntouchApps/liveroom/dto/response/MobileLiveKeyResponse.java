package com.intouch.IntouchApps.liveroom.dto.response;

import com.intouch.IntouchApps.liveroom.LiveKeyBuildStatus;
import com.intouch.IntouchApps.liveroom.LiveKeyType;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileLiveKeyResponse {

    private Long id;

    private String keyValue;
    private LiveKeyType keyType;

    private Integer assignedOrder;

    private Integer currentRow;
    private Integer currentColumn;

    private Integer targetRow;
    private Integer targetColumn;

    private LiveKeyBuildStatus status;
    private String keyFamilyId;
}