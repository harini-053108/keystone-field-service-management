package com.meridian.keystone.dto;

import com.meridian.keystone.domain.PartUsage;

import java.math.BigDecimal;
import java.time.Instant;

public record PartUsageDto(
        Long id,
        Long partId,
        String partCode,
        String partName,
        int quantity,
        BigDecimal unitCost,
        BigDecimal totalCost,
        String note,
        Instant createdAt) {

    public static PartUsageDto from(PartUsage pu) {
        return new PartUsageDto(
                pu.getId(),
                pu.getPart().getId(),
                pu.getPart().getCode(),
                pu.getPart().getName(),
                pu.getQuantity(),
                pu.getUnitCost(),
                pu.getUnitCost().multiply(BigDecimal.valueOf(pu.getQuantity())),
                pu.getNote(),
                pu.getCreatedAt());
    }
}