package com.intouch.IntouchApps.liveroom.dto;

import com.intouch.IntouchApps.liveroom.LiveKeyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveKeyRequest {

    @NotBlank(message = "Key value is required")
    private String keyValue;

    @NotNull(message = "Key type is required")
    private LiveKeyType keyType;
    @NotBlank(message = "keyFamilyId is required")
    private String keyFamilyId;
    @NotNull(message = "Target row is required")
    private Integer targetRow;

    @NotNull(message = "Target column is required")
    private Integer targetColumn;

    @NotNull(message = "Sort order is required")
    private Integer sortOrder;

}