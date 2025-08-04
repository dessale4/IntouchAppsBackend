package com.intouch.IntouchApps.user;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountDTO {
    private String accountEmail;
    private String accountUsername;
}
