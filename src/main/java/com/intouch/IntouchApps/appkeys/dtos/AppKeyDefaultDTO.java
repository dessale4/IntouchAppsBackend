package com.intouch.IntouchApps.appkeys.dtos;

import lombok.*;

//@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppKeyDefaultDTO {
    private Integer keyId;
    private Integer keyFamilyId;
    private String keyName;
    private String keyInEnglish;
    private KeyExampleDTO defaultKeyExample;
    private KeyAudioDTO defaultKeyAudio;
}
