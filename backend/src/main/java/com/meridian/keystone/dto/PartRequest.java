package com.meridian.keystone.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PartRequest(
        @NotBlank(message = "Code is required")
        @Size(max = 64, message = "Code must be at most 64 characters")
        String code,

        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name must be at most 255 characters")
        String name,

        @NotNull(message = "Unit cost is required")
        @DecimalMin(value = "0.00", message = "Unit cost cannot be negative")
        BigDecimal unitCost,

        @NotNull(message = "Stock quantity is required")
        @Min(value = 0, message = "Stock cannot be negative")
        Integer stockQty) {
}