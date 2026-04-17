package com.intouch.IntouchApps.user;

import com.intouch.IntouchApps.role.Role;
import lombok.*;

import java.time.LocalDate;
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
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private boolean accountLocked;
    private boolean enabled;
    private Integer screensLockPin;
    private Set<String> roles;
    //    private Set<AppAccessDTO> appAccess;
    private Set<String> appAccesses;
//    private boolean isSubscribed;
//    private LocalDateTime subscriptionEndDate;
    private String username;
    private Boolean paymentEnabled;
    private AgePolicyResponse agePolicy;
}
