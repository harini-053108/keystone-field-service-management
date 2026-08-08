package com.meridian.keystone.service;

import com.meridian.keystone.domain.Part;
import com.meridian.keystone.domain.PartUsage;
import com.meridian.keystone.domain.User;
import com.meridian.keystone.domain.WorkOrder;
import com.meridian.keystone.dto.PartUsageDto;
import com.meridian.keystone.dto.PartUsageRequest;
import com.meridian.keystone.exception.ApiException;
import com.meridian.keystone.repository.PartRepository;
import com.meridian.keystone.repository.PartUsageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class PartUsageService {

    private final PartUsageRepository partUsageRepository;
    private final PartRepository partRepository;
    private final WorkOrderService workOrderService;

    public PartUsageService(PartUsageRepository partUsageRepository, PartRepository partRepository,
                            WorkOrderService workOrderService) {
        this.partUsageRepository = partUsageRepository;
        this.partRepository = partRepository;
        this.workOrderService = workOrderService;
    }

    /**
     * Log a part against a work order and decrement stock in the SAME transaction.
     * Stock is validated before decrement so it can never go negative.
     */
    @Transactional
    public PartUsageDto log(Long workOrderId, PartUsageRequest request, User actor) {
        WorkOrder wo = workOrderService.loadScoped(workOrderId, actor);
        requireNonTerminal(wo);
        Part part = partRepository.findById(request.partId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Part not found"));

        if (part.getStockQty() < request.quantity()) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Insufficient stock for part " + part.getCode() + " (available " + part.getStockQty() + ")");
        }

        // Decrement stock + create the usage line atomically.
        part.setStockQty(part.getStockQty() - request.quantity());
        partRepository.save(part);

        PartUsage usage = new PartUsage();
        usage.setWorkOrder(wo);
        usage.setPart(part);
        usage.setQuantity(request.quantity());
        usage.setUnitCost(part.getUnitCost());
        usage.setNote(request.note());
        usage.setCreatedAt(Instant.now());
        PartUsage saved = partUsageRepository.save(usage);

        return PartUsageDto.from(saved);
    }

    private void requireNonTerminal(WorkOrder wo) {
        if (wo.getStatus().isTerminal()) {
            throw new ApiException(HttpStatus.CONFLICT, "Cannot log parts on a " + wo.getStatus() + " work order");
        }
    }
}