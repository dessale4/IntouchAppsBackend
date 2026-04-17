package com.intouch.IntouchApps.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RemoveRoleRequest {

    @NotNull(message = "User id is required")
    private Integer userId;

    @NotNull(message = "Role id is required")
    private Integer roleId;
}
