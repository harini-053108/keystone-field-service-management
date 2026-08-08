package com.meridian.keystone.dto;

import com.meridian.keystone.domain.Role;

public record AuthResponse(
        String token,
        String tokenType,
        Long id,
        String email,
        String fullName,
        Role role,
        Long customerId) {
}