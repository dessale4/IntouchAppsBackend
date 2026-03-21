package com.intouch.IntouchApps.appkeys.dtos;

import lombok.*;

import java.io.Serializable;

//@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KeyFamilyDefaultDTO implements Serializable {
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
