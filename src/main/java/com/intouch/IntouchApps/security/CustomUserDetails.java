package com.intouch.IntouchApps.security;

import com.intouch.IntouchApps.user.User;
import com.intouch.IntouchApps.user.UserRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;
@Getter
public class CustomUserDetails implements UserDetails {
    private final Integer userId;
    private final String username;
    private final String fullName;
    private final String email;
    private final String password;
    private boolean accountLocked;
    private boolean enabled;
    private final List<GrantedAuthority> authorities;
    public CustomUserDetails(User user) {
        this.userId = user.getId();
        this.username = user.getUserName();
        this.email = user.getEmail();
        this.fullName = user.fullName();
        this.password = user.getPassword();
        this.accountLocked = user.isAccountLocked();
        this.enabled = user.isEnabled();
        this.authorities = user.getUserRoles().stream()
                .filter(UserRole::isActive)
                .map(userRole -> (GrantedAuthority)new SimpleGrantedAuthority(userRole.getRole().getName()))
                .toList();
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    @Override
    public String getPassword() {
        return password;
    }
    /**
     * We use email as login credential.
     */
    @Override
    public String getUsername() {
        return email;
    }
    public String getDisplayUsername() {
        return username;
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
}
