package com.intouch.IntouchApps.appkeys.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.*;

//@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KeyExampleDTO {
    private Integer id;
    private String englishName;
    private String tigrinyaName;
    private String amharicName;
    private String category;
    private Integer keyId;
    private Integer keyFamilyId;
    private boolean isDefault;
    private String imageUrl;
    private String imageFileName;
    private String audioFileName;
    private String audioUrl;
}
