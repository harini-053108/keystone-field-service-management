package com.meridian.keystone.dto;

import com.meridian.keystone.domain.WorkOrderStatus;
import com.meridian.keystone.domain.WorkOrderStatusHistory;

import java.time.Instant;

public record StatusHistoryDto(
        Long id,
        WorkOrderStatus fromStatus,
        WorkOrderStatus toStatus,
        Long changedById,
        String changedByName,
        String note,
        Instant changedAt) {

    public static StatusHistoryDto from(WorkOrderStatusHistory h) {
        Long uid = h.getChangedBy() != null ? h.getChangedBy().getId() : null;
        String name = h.getChangedBy() != null ? h.getChangedBy().getFullName() : null;
        return new StatusHistoryDto(h.getId(), h.getFromStatus(), h.getToStatus(), uid, name,
                h.getNote(), h.getChangedAt());
    }
}