package com.intouch.IntouchApps.user;

import com.intouch.IntouchApps.role.Role;
//import com.intouch.IntouchApps.security.StringEncryptConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnTransformer;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Users_TBL")

public class User implements UserDetails, Principal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String firstName;
    private String lastName;
//    private LocalDate dateOfBirth;
    @Column(unique = true)
    private String email;
    @Column(unique = true)
    private String publicUserName;
    private String password;
    private boolean accountLocked;
    private boolean enabled;
//    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;
//    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime lastModifiedDate;
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles = new HashSet<>();//No role at first
//    private boolean isSubscribed;
//    private LocalDateTime subscriptionEndDate;
    @Override
    public String getName() {
        return email;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream()
                .map(r->new SimpleGrantedAuthority(r.getName()))
                .collect(Collectors.toList());
    }
    @Override
    public String getPassword() {
        return password;
    }
    @Override
    public String getUsername() {
        return email;
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    public String fullName(){
        return firstName + " " + lastName;
    }
    public void addRole(Role role){
        if(role.getName().equals("") || role.getName() == null)
            throw new IllegalArgumentException("Not allowed user role");
       getRoles().add(role);
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
                ", roles=" + roles +
//                ", isSubscribed=" + isSubscribed +
//                ", subscriptionEndDate=" + subscriptionEndDate +
                '}';
    }
}
