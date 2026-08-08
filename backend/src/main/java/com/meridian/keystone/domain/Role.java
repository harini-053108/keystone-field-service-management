package com.meridian.keystone.domain;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    DISPATCHER,
    TECHNICIAN,
    MANAGER,
    CUSTOMER;

    @Override
    public String getAuthority() {
        return "ROLE_" + name();
    }

    public static Role fromName(String value) {
        return Role.valueOf(value.toUpperCase());
    }
}