package com.intouch.IntouchApps.appkeys;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KeyFamilyResponse {
    private Integer keyFamilyId;
    private KeyFamilyAudio defaultKeyFamilyAudio;
    private AppKeyResponse keyOne;
    private AppKeyResponse keyTwo;
    private AppKeyResponse keyThree;
    private AppKeyResponse keyFour;
    private AppKeyResponse keyFive;
    private AppKeyResponse keySix;
    private AppKeyResponse keySeven;
}
