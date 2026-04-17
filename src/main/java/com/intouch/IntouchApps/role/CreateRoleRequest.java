package com.intouch.IntouchApps.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateRoleRequest {

    @NotBlank(message = "Role name is required")
    private String name;
}
