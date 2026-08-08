package com.meridian.keystone.domain;

public enum SlaStatus {
    ON_TRACK,
    AT_RISK,
    BREACHED,
    /** Terminal / completed orders that are not evaluated against SLA. */
    NOT_APPLICABLE
}