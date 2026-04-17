package com.intouch.IntouchApps.user;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserRoleResponse {
    private Integer userId;
    private String username;
    private Integer roleId;
    private String roleName;
    private String assignedBy;
    private Instant assignedAt;
}