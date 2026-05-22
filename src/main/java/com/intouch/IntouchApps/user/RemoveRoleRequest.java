package com.intouch.IntouchApps.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RemoveRoleRequest {

    @NotNull(message = "User id is required")
    private Integer userId;

    @NotBlank(message = "Role name is required")
    private String roleName;
}
