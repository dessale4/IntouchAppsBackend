package com.intouch.IntouchApps.appkeys.dtos;

import jakarta.persistence.Column;
import lombok.*;

//@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KeyAudioDTO {
    private Integer id;
    private Integer keyFamilyId;
    private Integer keyId;
    private String keyAudioUrl;
    private String keyAudioFileName;
    private boolean isDefault;
    private String keyAudioTimeStamp;
}
