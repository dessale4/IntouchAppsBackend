package com.intouch.IntouchApps.liveroom.dto.response;

import com.intouch.IntouchApps.liveroom.ParticipantStatus;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerLiveRoomParticipantDetailResponse {

    private Long roomId;
    private Long groupId;
    private String groupName;

    private Long participantId;
    private String displayName;
    private String participantCode;
    private ParticipantStatus status;

    private Long completedKeys;
    private Long remainingKeys;
    private Long totalAssignedKeys;

    private MobileLiveKeyResponse waitingKey;

    private List<MobileLiveKeyResponse> placedKeys;
    private List<MobileLiveKeyResponse> remainingAssignedKeys;
}
