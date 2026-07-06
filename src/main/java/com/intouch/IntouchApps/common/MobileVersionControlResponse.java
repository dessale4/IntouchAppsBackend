package com.intouch.IntouchApps.common;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MobileVersionControlResponse {
    private String latestVersion;
    private String minimumSupportedVersion;
    private String updateMessage;
    private String androidUrl;
    private String iosUrl;
}
