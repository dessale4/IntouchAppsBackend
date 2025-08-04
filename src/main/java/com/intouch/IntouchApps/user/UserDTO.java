package com.intouch.IntouchApps.user;

import com.intouch.IntouchApps.role.Role;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private String firstName;
    private String lastName;
    private String email;
    private boolean accountLocked;
    private boolean enabled;
    private List<Role> roles;
    private boolean isSubscribed;
    private LocalDateTime subscriptionEndDate;
    private String username;
}
