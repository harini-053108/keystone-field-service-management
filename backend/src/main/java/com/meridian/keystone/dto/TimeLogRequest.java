package com.meridian.keystone.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TimeLogRequest(
        @NotNull(message = "Minutes is required")
        @Min(value = 1, message = "Minutes must be at least 1")
        @Max(value = 1440, message = "Minutes cannot exceed 1440")
        Integer minutes,

        @Size(max = 1000, message = "Note must be at most 1000 characters")
        String note) {
}