package com.intouch.IntouchApps.liveroom.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkCreateParticipantSlotsRequest {

    @NotBlank(message = "Base participant name is required")
    private String baseName;

    @NotNull(message = "Participant count is required")
    @Min(value = 1, message = "Participant count must be at least 1")
    private Integer count;
}
