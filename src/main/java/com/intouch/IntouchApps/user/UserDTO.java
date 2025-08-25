package com.intouch.IntouchApps.user;

import com.intouch.IntouchApps.role.Role;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

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
    //    private Set<Role> roles;
    private Set<String> roles;
    //    private Set<AppAccessDTO> appAccess;
    private Set<String> appAccesses;
    private boolean isSubscribed;
    private LocalDateTime subscriptionEndDate;
    private String username;
    private Boolean paymentEnabled;
}
