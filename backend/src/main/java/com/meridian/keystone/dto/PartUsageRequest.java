package com.meridian.keystone.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PartUsageRequest(
        @NotNull(message = "Part is required")
        Long partId,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 100000, message = "Quantity is too large")
        Integer quantity,

        @Size(max = 1000, message = "Note must be at most 1000 characters")
        String note) {
}