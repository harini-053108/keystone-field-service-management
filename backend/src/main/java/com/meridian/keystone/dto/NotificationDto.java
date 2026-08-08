package com.meridian.keystone.dto;

import com.meridian.keystone.domain.Notification;

import java.time.Instant;

public record NotificationDto(
        Long id,
        String message,
        String type,
        Long workOrderId,
        boolean read,
        Instant createdAt) {

    public static NotificationDto from(Notification n) {
        Long woId = n.getWorkOrder() != null ? n.getWorkOrder().getId() : null;
        return new NotificationDto(n.getId(), n.getMessage(), n.getType(), woId, n.isRead(), n.getCreatedAt());
    }
}