package com.meridian.keystone.dto;

import com.meridian.keystone.domain.Part;

import java.math.BigDecimal;
import java.time.Instant;

public record PartDto(
        Long id,
        String code,
        String name,
        BigDecimal unitCost,
        int stockQty,
        Instant createdAt) {

    public static PartDto from(Part part) {
        return new PartDto(part.getId(), part.getCode(), part.getName(),
                part.getUnitCost(), part.getStockQty(), part.getCreatedAt());
    }
}