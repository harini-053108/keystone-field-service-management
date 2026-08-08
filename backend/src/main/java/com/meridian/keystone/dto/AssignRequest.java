package com.meridian.keystone.dto;

import jakarta.validation.constraints.NotNull;

public record AssignRequest(
        @NotNull(message = "Technician is required")
        Long technicianId) {
}