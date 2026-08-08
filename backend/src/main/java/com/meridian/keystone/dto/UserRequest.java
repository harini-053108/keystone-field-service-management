package com.meridian.keystone.dto;

import com.meridian.keystone.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank(message = "Full name is required")
        @Size(max = 255, message = "Name must be at most 255 characters")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotNull(message = "Role is required")
        Role role,

        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
        String password,

        Long customerId) {
}