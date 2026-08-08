package com.meridian.keystone.service;

import com.meridian.keystone.domain.SlaStatus;
import com.meridian.keystone.domain.WorkOrder;
import com.meridian.keystone.domain.WorkOrderStatus;
import com.meridian.keystone.repository.WorkOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Scheduled job that flags open work orders that are at risk of, or in breach of, their SLA.
 * A notification is raised for managers only when an order newly crosses into BREACHED.
 */
@Service
@ConditionalOnProperty(name = "app.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class SlaBreachScheduler {

    private static final Logger log = LoggerFactory.getLogger(SlaBreachScheduler.class);

    private final WorkOrderRepository workOrderRepository;
    private final SlaService slaService;
    private final NotificationService notificationService;

    public SlaBreachScheduler(WorkOrderRepository workOrderRepository, SlaService slaService,
                              NotificationService notificationService) {
        this.workOrderRepository = workOrderRepository;
        this.slaService = slaService;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedDelayString = "${app.scheduler.interval-ms:600000}", initialDelay = 60000)
    @Transactional
    public void evaluateSla() {
        Instant now = Instant.now();
        List<WorkOrder> open = workOrderRepository.findByStatusNotIn(
                List.of(WorkOrderStatus.CLOSED, WorkOrderStatus.CANCELLED, WorkOrderStatus.COMPLETED));
        for (WorkOrder wo : open) {
            SlaStatus previous = wo.getSlaStatus();
            SlaStatus current = slaService.evaluate(wo, now);
            if (previous != current) {
                wo.setSlaStatus(current);
                workOrderRepository.save(wo);
                log.info("SLA status for {} changed {} -> {}", wo.getCode(), previous, current);
                if (current == SlaStatus.BREACHED) {
                    notificationService.slaBreach(wo);
                } else if (current == SlaStatus.AT_RISK) {
                    notificationService.slaAtRisk(wo);
                }
            }
        }
    }
}