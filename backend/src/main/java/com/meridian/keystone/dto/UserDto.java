package com.meridian.keystone.dto;

import com.meridian.keystone.domain.Role;
import com.meridian.keystone.domain.User;

import java.time.Instant;

public record UserDto(
        Long id,
        String fullName,
        String email,
        Role role,
        Long customerId,
        boolean enabled,
        Instant createdAt) {

    public static UserDto from(User user) {
        Long customerId = user.getCustomer() != null ? user.getCustomer().getId() : null;
        return new UserDto(user.getId(), user.getFullName(), user.getEmail(), user.getRole(),
                customerId, user.isEnabled(), user.getCreatedAt());
    }
}