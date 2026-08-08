package com.meridian.keystone.dto;

import com.meridian.keystone.domain.Priority;
import com.meridian.keystone.domain.SlaStatus;
import com.meridian.keystone.domain.WorkOrder;
import com.meridian.keystone.domain.WorkOrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record WorkOrderDto(
        Long id,
        String code,
        String title,
        String description,
        Priority priority,
        WorkOrderStatus status,
        SlaStatus slaStatus,
        Instant slaDueAt,
        Long customerId,
        String customerName,
        Long siteId,
        String siteName,
        Long assigneeId,
        String assigneeName,
        Instant createdAt,
        Instant updatedAt,
        Instant completedAt,
        int totalMinutes,
        BigDecimal totalPartsCost,
        List<PartUsageDto> partUsages,
        List<TimeLogDto> timeLogs,
        List<StatusHistoryDto> history) {

    public static WorkOrderDto from(WorkOrder wo) {
        return from(wo, List.of(), List.of(), List.of(), BigDecimal.ZERO, 0);
    }

    public static WorkOrderDto from(WorkOrder wo,
                                    List<PartUsageDto> partUsages,
                                    List<TimeLogDto> timeLogs,
                                    List<StatusHistoryDto> history,
                                    BigDecimal totalPartsCost,
                                    int totalMinutes) {
        Long customerId = wo.getCustomer() != null ? wo.getCustomer().getId() : null;
        String customerName = wo.getCustomer() != null ? wo.getCustomer().getName() : null;
        Long siteId = wo.getSite() != null ? wo.getSite().getId() : null;
        String siteName = wo.getSite() != null ? wo.getSite().getName() : null;
        Long assigneeId = wo.getAssignee() != null ? wo.getAssignee().getId() : null;
        String assigneeName = wo.getAssignee() != null ? wo.getAssignee().getFullName() : null;

        return new WorkOrderDto(
                wo.getId(), wo.getCode(), wo.getTitle(), wo.getDescription(),
                wo.getPriority(), wo.getStatus(), wo.getSlaStatus(), wo.getSlaDueAt(),
                customerId, customerName, siteId, siteName, assigneeId, assigneeName,
                wo.getCreatedAt(), wo.getUpdatedAt(), wo.getCompletedAt(),
                totalMinutes, totalPartsCost, partUsages, timeLogs, history);
    }
}