package com.intouch.IntouchApps.user;

import com.intouch.IntouchApps.role.Role;
//import com.intouch.IntouchApps.security.StringEncryptConverter;
import com.intouch.IntouchApps.validation.ValidPublicUserName;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
//@ToString(exclude = "userRoles")
//@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "Users_TBL")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String firstName;
    private String lastName;
    //fix this later
    @Column(name = "date_of_birth", nullable = true)
//    @Column(name = "date_of_birth", nullable = false)
//    @NotNull(message = "Date of birth is required")
//    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    @Column(name = "dob_verified")
    @Builder.Default
    private Boolean dobVerified = false;
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    @Column(name = "phone_verified")
    @Builder.Default
    private Boolean phoneVerified = false;
    @Column(unique = true)
    private String email;
    @Column(unique = true)
    @ValidPublicUserName
    private String userName;
    private String password;
    private boolean accountLocked;
    private boolean enabled;
    private Integer screensLockPin;
    //    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdDate;
    //    @LastModifiedDate
    @Column(nullable = false)
    private Instant lastModifiedDate;
//    @Column(nullable = false)
//    @Builder.Default
//    private String phoneNumber ="0000000000";
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<UserRole> userRoles = new HashSet<>();
    private Instant lastLoginAt;
    @Builder.Default
    @Column(name="accountClosed")
    private Boolean requestedAccountClosure = false;
    @Builder.Default
    @Column(name = "modifiedBy")
    private String accountModifiedBy = "SYSTEM";
    public void addUserRole(UserRole userRole) {
        userRoles.add(userRole);
        userRole.setUser(this);
    }

    public void removeUserRole(UserRole userRole) {
        userRoles.remove(userRole);
        userRole.setUser(null);
    }

    public String fullName() {
        return this.firstName + " " + this.lastName;
    }

    @Override
    public String toString() {
        return "User{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", accountLocked=" + accountLocked +
                ", enabled=" + enabled +
                ", createdDate=" + createdDate +
//                ", roles=" + roles +
//                ", isSubscribed=" + isSubscribed +
//                ", subscriptionEndDate=" + subscriptionEndDate +
                '}';
    }
}
