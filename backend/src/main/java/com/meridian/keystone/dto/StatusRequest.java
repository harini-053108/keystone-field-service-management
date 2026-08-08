package com.meridian.keystone.dto;

import com.meridian.keystone.domain.WorkOrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StatusRequest(
        @NotNull(message = "Status is required")
        WorkOrderStatus status,

        @Size(max = 1000, message = "Note must be at most 1000 characters")
        String note) {
}