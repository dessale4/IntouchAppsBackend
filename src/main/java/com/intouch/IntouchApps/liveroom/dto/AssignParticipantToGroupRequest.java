package com.intouch.IntouchApps.liveroom.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignParticipantToGroupRequest {

    @NotNull(message = "Participant id is required")
    private Long participantId;

    @NotNull(message = "Group id is required")
    private Long groupId;
}