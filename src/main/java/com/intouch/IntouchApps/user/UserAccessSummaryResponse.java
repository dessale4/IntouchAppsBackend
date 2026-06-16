package com.intouch.IntouchApps.user;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAccessSummaryResponse {

    private boolean hasBasicAccess;

    private Instant accessExpiresAt;

    private boolean eligibleForRenewal;
}
