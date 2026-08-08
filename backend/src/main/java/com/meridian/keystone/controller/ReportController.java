package com.meridian.keystone.controller;

import com.meridian.keystone.dto.ReportSummary;
import com.meridian.keystone.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Manager dashboard metrics")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('MANAGER')")
    @Operation(summary = "Aggregated operational metrics for the manager dashboard")
    public ReportSummary summary(@RequestParam(required = false) Long customerId,
                                 @RequestParam(required = false) Long assigneeId) {
        return reportService.summary(customerId, assigneeId);
    }
}