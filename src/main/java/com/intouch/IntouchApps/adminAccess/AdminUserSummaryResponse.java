package com.intouch.IntouchApps.adminAccess;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserSummaryResponse {
    private Integer id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Boolean enabled;
    private List<String> roles;
}
