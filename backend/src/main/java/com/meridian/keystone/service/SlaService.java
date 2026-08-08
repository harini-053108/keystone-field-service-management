package com.meridian.keystone.service;

import com.meridian.keystone.domain.SlaStatus;
import com.meridian.keystone.domain.WorkOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/** Computes SLA health for a work order. */
@Service
public class SlaService {

    private final double riskFraction;

    public SlaService(@Value("${app.sla.risk-threshold-fraction:0.25}") double riskFraction) {
        this.riskFraction = riskFraction;
    }

    /**
     * Evaluate an OPEN work order:
     * BREACHED when the due date has passed; AT_RISK within the tail of the window; else ON_TRACK.
     */
    public SlaStatus evaluate(WorkOrder wo, Instant now) {
        if (wo.getSlaDueAt() == null) {
            return SlaStatus.ON_TRACK;
        }
        if (now.isAfter(wo.getSlaDueAt())) {
            return SlaStatus.BREACHED;
        }
        Duration window = wo.getPriority().getSlaWindow();
        Duration remaining = Duration.between(now, wo.getSlaDueAt());
        if (window.isZero()) {
            return SlaStatus.ON_TRACK;
        }
        long tailMillis = (long) (window.toMillis() * riskFraction);
        if (remaining.toMillis() <= tailMillis) {
            return SlaStatus.AT_RISK;
        }
        return SlaStatus.ON_TRACK;
    }

    /** Convenience evaluate at "now". */
    public SlaStatus evaluate(WorkOrder wo) {
        return evaluate(wo, Instant.now());
    }
}