package com.intouch.IntouchApps.role;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleResponse {
    private Integer id;
    private String name;
}
