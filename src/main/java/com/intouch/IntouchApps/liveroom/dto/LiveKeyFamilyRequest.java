package com.intouch.IntouchApps.liveroom.dto;

import com.intouch.IntouchApps.liveroom.LiveKeyType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveKeyFamilyRequest {

    @NotBlank(message = "Family code is required")
    private String familyCode;

    @NotBlank(message = "Family name is required")
    private String familyName;

    @NotNull(message = "Row index is required")
    private Integer rowIndex;

    @NotNull(message = "Column count is required")
    private Integer columnCount;

    @NotNull(message = "Key type is required")
    private LiveKeyType keyType;

    @Valid
    private List<LiveKeyRequest> keys;
}