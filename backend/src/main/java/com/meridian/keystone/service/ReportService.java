package com.meridian.keystone.service;

import com.meridian.keystone.domain.SlaStatus;
import com.meridian.keystone.domain.WorkOrderStatus;
import com.meridian.keystone.dto.ReportSummary;
import com.meridian.keystone.dto.ReportSummary.NameCount;
import com.meridian.keystone.repository.WorkOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final WorkOrderRepository workOrderRepository;

    public ReportService(WorkOrderRepository workOrderRepository) {
        this.workOrderRepository = workOrderRepository;
    }

    @Transactional(readOnly = true)
    public ReportSummary summary(Long customerId, Long assigneeId) {
        Map<WorkOrderStatus, Long> byStatus = new EnumMap<>(WorkOrderStatus.class);
        for (Object[] row : workOrderRepository.countByStatusGrouped(customerId, assigneeId)) {
            byStatus.put((WorkOrderStatus) row[0], (Long) row[1]);
        }

        Map<SlaStatus, Long> bySla = new EnumMap<>(SlaStatus.class);
        for (Object[] row : workOrderRepository.countBySlaStatusGrouped(customerId, assigneeId)) {
            bySla.put((SlaStatus) row[0], (Long) row[1]);
        }

        Map<SlaStatus, Long> openSla = new EnumMap<>(SlaStatus.class);
        for (Object[] row : workOrderRepository.countOpenSlaStatusGrouped(customerId, assigneeId)) {
            openSla.put((SlaStatus) row[0], (Long) row[1]);
        }

        long totalOpen = byStatus.entrySet().stream()
                .filter(e -> !e.getKey().isTerminal())
                .mapToLong(Map.Entry::getValue)
                .sum();
        long overdue = workOrderRepository.countOverdue(customerId, assigneeId);
        long breached = openSla.getOrDefault(SlaStatus.BREACHED, 0L);

        long onTime = workOrderRepository.countCompletedOnTime(customerId, assigneeId);
        long terminalWithSla = workOrderRepository.countTerminalWithSla(customerId, assigneeId);
        double compliance = terminalWithSla == 0 ? 0.0 : (100.0 * onTime / terminalWithSla);

        List<NameCount> byTechnician = workOrderRepository.countByTechnician(customerId, assigneeId).stream()
                .map(r -> new NameCount((String) r[1], (Long) r[2]))
                .toList();
        List<NameCount> bySite = workOrderRepository.countBySite(customerId, assigneeId).stream()
                .map(r -> new NameCount((String) r[0], (Long) r[1]))
                .toList();

        return new ReportSummary(totalOpen, overdue, breached, compliance, byStatus, bySla, byTechnician, bySite);
    }
}