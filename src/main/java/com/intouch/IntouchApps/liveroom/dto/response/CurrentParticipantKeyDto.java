package com.intouch.IntouchApps.liveroom.dto.response;

public record CurrentParticipantKeyDto(
        Long participantId,
        String participantName,
        String participantCode,
        String currentKey,
        Integer assignedOrder
) {}
