package com.intouch.IntouchApps.appkeys;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppKeyRequest {
    private Integer keyFamilyId;
    private Integer keyId;
    private String keyName;
    private String keyNameInEnglish;
}
