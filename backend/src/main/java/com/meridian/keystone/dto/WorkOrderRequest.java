package com.meridian.keystone.dto;

import com.meridian.keystone.domain.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WorkOrderRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must be at most 255 characters")
        String title,

        @Size(max = 4000, message = "Description must be at most 4000 characters")
        String description,

        @NotNull(message = "Priority is required")
        Priority priority,

        @NotNull(message = "Customer is required")
        Long customerId,

        @NotNull(message = "Site is required")
        Long siteId) {
}