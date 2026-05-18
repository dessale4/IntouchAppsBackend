package com.intouch.IntouchApps.liveroom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinRoomRequest {

    @NotBlank(message = "Room code is required")
    @Size(min = 6, max = 6, message = "Room code must be 6 characters")
    private String roomCode;

    @NotBlank(message = "Participant code is required")
    @Size(min = 4, max = 4, message = "Participant code must be 4 digits")
    private String participantCode;
}