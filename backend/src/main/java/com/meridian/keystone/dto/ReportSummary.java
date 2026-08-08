package com.meridian.keystone.dto;

import com.meridian.keystone.domain.SlaStatus;
import com.meridian.keystone.domain.WorkOrderStatus;

import java.util.List;
import java.util.Map;

/** Aggregated operational metrics for the manager dashboard. */
public record ReportSummary(
        long totalOpen,
        long overdue,
        long breached,
        double slaCompliancePercent,
        Map<WorkOrderStatus, Long> countsByStatus,
        Map<SlaStatus, Long> countsBySla,
        List<NameCount> byTechnician,
        List<NameCount> bySite) {

    public record NameCount(String name, long count) {
    }
}