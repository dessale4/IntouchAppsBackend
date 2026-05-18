package com.intouch.IntouchApps.liveroom.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddParticipantSlotRequest {

    @NotBlank(message = "Display name is required")
    private String displayName;
}