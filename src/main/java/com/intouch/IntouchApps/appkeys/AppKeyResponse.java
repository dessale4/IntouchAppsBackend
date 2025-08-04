package com.intouch.IntouchApps.appkeys;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AppKeyResponse {
    private Integer keyId;
    private Integer keyFamilyId;
    private String keyName;
    private String keyInEnglish;
    private KeyExample defaultKeyExample;
    private KeyAudio defaultKeyAudio;
}
