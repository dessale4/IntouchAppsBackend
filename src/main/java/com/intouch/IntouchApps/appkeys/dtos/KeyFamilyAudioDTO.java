package com.intouch.IntouchApps.appkeys.dtos;

import jakarta.persistence.Column;
import lombok.*;


//@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KeyFamilyAudioDTO {
    private Integer id;
    private Integer keyFamilyId;
    private String keyFamilyAudioUrl;
    private String keyFamilyAudioFileName;
    private boolean isDefault;
    private String keyOneTimeStamp;
    private String keyTwoTimeStamp;
    private String keyThreeTimeStamp;
    private String keyFourTimeStamp;
    private String keyFiveTimeStamp;
    private String keySixTimeStamp;
    private String keySevenTimeStamp;
}
