package com.meridian.keystone.service;

import com.meridian.keystone.domain.Role;
import com.meridian.keystone.domain.WorkOrderStatus;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * The governed work-order lifecycle.
 *
 * NEW -> ASSIGNED -> IN_PROGRESS -> COMPLETED -> CLOSED
 * IN_PROGRESS <-> ON_HOLD
 * NEW/ASSIGNED -> CANCELLED
 * Terminal: CLOSED, CANCELLED.
 *
 * Only the transitions below are permitted; every other change is rejected with 409.
 */
public final class WorkOrderStateMachine {

    private static final Map<WorkOrderStatus, Set<WorkOrderStatus>> ALLOWED = new EnumMap<>(WorkOrderStatus.class);

    static {
        ALLOWED.put(WorkOrderStatus.NEW, Set.of(WorkOrderStatus.ASSIGNED, WorkOrderStatus.CANCELLED));
        ALLOWED.put(WorkOrderStatus.ASSIGNED, Set.of(WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.CANCELLED));
        ALLOWED.put(WorkOrderStatus.IN_PROGRESS, Set.of(WorkOrderStatus.ON_HOLD, WorkOrderStatus.COMPLETED));
        ALLOWED.put(WorkOrderStatus.ON_HOLD, Set.of(WorkOrderStatus.IN_PROGRESS));
        ALLOWED.put(WorkOrderStatus.COMPLETED, Set.of(WorkOrderStatus.CLOSED));
        ALLOWED.put(WorkOrderStatus.CLOSED, Set.of());
        ALLOWED.put(WorkOrderStatus.CANCELLED, Set.of());
    }

    private WorkOrderStateMachine() {
    }

    public static boolean allows(WorkOrderStatus from, WorkOrderStatus to) {
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }

    /**
     * Role rule for a transition, independent of who is assigned.
     * Returns the role(s) permitted to perform it. Where the rule is "the assigned
     * technician", returns TECHNICIAN and the caller must additionally check assignment.
     */
    public static boolean isTechnicianAction(WorkOrderStatus from, WorkOrderStatus to) {
        return switch (to) {
            case IN_PROGRESS, ON_HOLD, COMPLETED -> true; // start / hold / resume / complete
            default -> false;
        };
    }

    /** CLOSE is manager-only. */
    public static boolean isManagerAction(WorkOrderStatus to) {
        return to == WorkOrderStatus.CLOSED;
    }

    /** Cancel & assign are dispatcher/manager actions. */
    public static boolean isDispatcherOrManagerAction(WorkOrderStatus to) {
        return to == WorkOrderStatus.CANCELLED;
    }

    public static Set<WorkOrderStatus> allowedTransitions(WorkOrderStatus from) {
        return ALLOWED.getOrDefault(from, Set.of());
    }
}