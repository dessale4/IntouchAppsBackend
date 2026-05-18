package com.intouch.IntouchApps.liveroom.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLiveRoomTemplateRequest {

    @Valid
    @NotEmpty(message = "At least one key family is required")
    private List<LiveKeyFamilyRequest> families;
}