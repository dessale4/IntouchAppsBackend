package com.intouch.IntouchApps.liveroom.dto.response;

import com.intouch.IntouchApps.liveroom.ParticipantStatus;

public record CurrentParticipantKeyDto(
        Long participantId,
        String participantName,
        String participantCode,
        ParticipantStatus participantStatus,
        String currentKey,
        Integer assignedOrder
) {}
