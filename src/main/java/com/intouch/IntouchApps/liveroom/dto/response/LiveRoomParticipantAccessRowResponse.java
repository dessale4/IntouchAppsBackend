package com.intouch.IntouchApps.liveroom.dto.response;

import com.intouch.IntouchApps.liveroom.ParticipantStatus;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveRoomParticipantAccessRowResponse {
    private Long participantId;
    private String participantDisplayName;
    private String participantCode;
    private ParticipantStatus status;

    private Long groupId;
    private String groupName;

    private Integer mobileUserId;
    private String mobileUsername;
    private Instant claimedAt;
}
