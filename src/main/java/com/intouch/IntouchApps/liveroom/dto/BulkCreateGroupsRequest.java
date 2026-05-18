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
public class BulkCreateGroupsRequest {

    @NotBlank(message = "Base group name is required")
    private String baseName;

    @NotNull(message = "Group count is required")
    @Min(value = 1, message = "Group count must be at least 1")
    private Integer count;
}
