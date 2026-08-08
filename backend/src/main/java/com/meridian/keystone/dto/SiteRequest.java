package com.meridian.keystone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SiteRequest(
        @NotBlank(message = "Site name is required")
        @Size(max = 255, message = "Site name must be at most 255 characters")
        String name,

        @Size(max = 2000, message = "Address must be at most 2000 characters")
        String address) {
}