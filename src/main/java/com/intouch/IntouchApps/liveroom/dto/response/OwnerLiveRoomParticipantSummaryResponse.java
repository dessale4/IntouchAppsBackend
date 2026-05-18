package com.intouch.IntouchApps.liveroom.dto.response;

import com.intouch.IntouchApps.liveroom.ParticipantStatus;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerLiveRoomParticipantSummaryResponse {
    private Long participantId;
    private String displayName;
    private String participantCode;
    private ParticipantStatus status;

    private Long completedKeys;
    private Long remainingKeys;

    private MobileLiveKeyResponse waitingKey;
}
