package com.meridian.keystone.service;

import com.meridian.keystone.domain.TimeLog;
import com.meridian.keystone.domain.User;
import com.meridian.keystone.domain.WorkOrder;
import com.meridian.keystone.dto.TimeLogDto;
import com.meridian.keystone.dto.TimeLogRequest;
import com.meridian.keystone.exception.ApiException;
import com.meridian.keystone.repository.TimeLogRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class TimeLogService {

    private final TimeLogRepository timeLogRepository;
    private final WorkOrderService workOrderService;

    public TimeLogService(TimeLogRepository timeLogRepository, WorkOrderService workOrderService) {
        this.timeLogRepository = timeLogRepository;
        this.workOrderService = workOrderService;
    }

    @Transactional
    public TimeLogDto log(Long workOrderId, TimeLogRequest request, User actor) {
        WorkOrder wo = workOrderService.loadScoped(workOrderId, actor);
        if (wo.getStatus().isTerminal()) {
            throw new ApiException(HttpStatus.CONFLICT, "Cannot log time on a " + wo.getStatus() + " work order");
        }
        TimeLog timeLog = new TimeLog();
        timeLog.setWorkOrder(wo);
        timeLog.setMinutes(request.minutes());
        timeLog.setNote(request.note());
        timeLog.setLoggedBy(actor);
        timeLog.setLoggedAt(Instant.now());
        return TimeLogDto.from(timeLogRepository.save(timeLog));
    }
}