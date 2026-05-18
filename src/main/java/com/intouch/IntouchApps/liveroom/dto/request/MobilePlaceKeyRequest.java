package com.intouch.IntouchApps.liveroom.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobilePlaceKeyRequest {

//    @NotNull(message = "Row index is required")
//    @Min(value = 0, message = "Row index cannot be negative")
//    private Integer rowIndex;

    @NotNull(message = "Column index is required")
    @Min(value = 1, message = "Column index must be at least 1")
    private Integer columnIndex;
}