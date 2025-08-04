package com.intouch.IntouchApps.appkeys;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeyFamilyAudioTimeStamp {
    @NotBlank
    private String keyOneTimeStamp;
    @NotBlank
    private String keyTwoTimeStamp;
    @NotBlank
    private String keyThreeTimeStamp;
    @NotBlank
    private String keyFourTimeStamp;
    @NotBlank
    private String keyFiveTimeStamp;
    @NotBlank
    private String keySixTimeStamp;
    @NotBlank
    private String keySevenTimeStamp;
}
