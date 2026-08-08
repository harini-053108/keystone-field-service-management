package com.meridian.keystone.domain;

import java.time.Duration;

/**
 * Priority drives the SLA target. Each priority maps to a response/resolution window
 * (in hours) used to compute a work order's SLA due date.
 */
public enum Priority {
    LOW(Duration.ofHours(72)),
    MEDIUM(Duration.ofHours(48)),
    HIGH(Duration.ofHours(24)),
    URGENT(Duration.ofHours(8));

    private final Duration slaWindow;

    Priority(Duration slaWindow) {
        this.slaWindow = slaWindow;
    }

    public Duration getSlaWindow() {
        return slaWindow;
    }
}