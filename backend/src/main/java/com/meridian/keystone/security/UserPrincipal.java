package com.meridian.keystone.security;

import com.meridian.keystone.domain.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/** Lightweight principal stored in the SecurityContext; never the JPA entity. */
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final Role role;
    private final Long customerId;
    private final boolean enabled;

    public UserPrincipal(Long id, String email, String password, Role role, Long customerId, boolean enabled) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.customerId = customerId;
        this.enabled = enabled;
    }

    public Long getId() { return id; }
    public Long getCustomerId() { return customerId; }
    public Role getRole() { return role; }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return List.of(role); }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return email; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return enabled; }
}