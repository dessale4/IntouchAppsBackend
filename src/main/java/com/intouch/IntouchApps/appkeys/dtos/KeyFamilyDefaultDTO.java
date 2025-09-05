package com.intouch.IntouchApps.appkeys.dtos;

import lombok.*;

//@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KeyFamilyDefaultDTO {
    private Integer keyFamilyId;
    private KeyFamilyAudioDTO defaultKeyFamilyAudio;
    private AppKeyDefaultDTO keyOne;
    private AppKeyDefaultDTO keyTwo;
    private AppKeyDefaultDTO keyThree;
    private AppKeyDefaultDTO keyFour;
    private AppKeyDefaultDTO keyFive;
    private AppKeyDefaultDTO keySix;
    private AppKeyDefaultDTO keySeven;
}
