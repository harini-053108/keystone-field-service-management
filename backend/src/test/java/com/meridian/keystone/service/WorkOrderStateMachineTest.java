package com.meridian.keystone.service;

import com.meridian.keystone.domain.Priority;
import com.meridian.keystone.domain.SlaStatus;
import com.meridian.keystone.domain.WorkOrder;
import com.meridian.keystone.domain.WorkOrderStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkOrderStateMachineTest {

    @Test
    void allowedTransitions() {
        assertTrue(WorkOrderStateMachine.allows(WorkOrderStatus.NEW, WorkOrderStatus.ASSIGNED));
        assertTrue(WorkOrderStateMachine.allows(WorkOrderStatus.NEW, WorkOrderStatus.CANCELLED));
        assertTrue(WorkOrderStateMachine.allows(WorkOrderStatus.ASSIGNED, WorkOrderStatus.IN_PROGRESS));
        assertTrue(WorkOrderStateMachine.allows(WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.ON_HOLD));
        assertTrue(WorkOrderStateMachine.allows(WorkOrderStatus.ON_HOLD, WorkOrderStatus.IN_PROGRESS));
        assertTrue(WorkOrderStateMachine.allows(WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.COMPLETED));
        assertTrue(WorkOrderStateMachine.allows(WorkOrderStatus.COMPLETED, WorkOrderStatus.CLOSED));
    }

    @Test
    void illegalTransitionsAreRejected() {
        assertFalse(WorkOrderStateMachine.allows(WorkOrderStatus.NEW, WorkOrderStatus.IN_PROGRESS));
        assertFalse(WorkOrderStateMachine.allows(WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.NEW));
        assertFalse(WorkOrderStateMachine.allows(WorkOrderStatus.COMPLETED, WorkOrderStatus.IN_PROGRESS));
        assertFalse(WorkOrderStateMachine.allows(WorkOrderStatus.ASSIGNED, WorkOrderStatus.COMPLETED));
    }

    @Test
    void terminalStatesHaveNoTransitions() {
        assertTrue(WorkOrderStatus.CLOSED.isTerminal());
        assertTrue(WorkOrderStatus.CANCELLED.isTerminal());
        assertEquals(0, WorkOrderStateMachine.allowedTransitions(WorkOrderStatus.CLOSED).size());
        assertEquals(0, WorkOrderStateMachine.allowedTransitions(WorkOrderStatus.CANCELLED).size());
    }

    @Test
    void technicianOwnsStartHoldComplete() {
        assertTrue(WorkOrderStateMachine.isTechnicianAction(WorkOrderStatus.ASSIGNED, WorkOrderStatus.IN_PROGRESS));
        assertTrue(WorkOrderStateMachine.isTechnicianAction(WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.ON_HOLD));
        assertTrue(WorkOrderStateMachine.isTechnicianAction(WorkOrderStatus.IN_PROGRESS, WorkOrderStatus.COMPLETED));
        assertFalse(WorkOrderStateMachine.isTechnicianAction(WorkOrderStatus.COMPLETED, WorkOrderStatus.CLOSED));
        assertTrue(WorkOrderStateMachine.isManagerAction(WorkOrderStatus.CLOSED));
    }
}

class SlaServiceTest {

    @Test
    void evaluatesBreachedAtRiskAndOnTrack() {
        SlaService sla = new SlaService(0.25);
        WorkOrder wo = new WorkOrder();
        wo.setPriority(Priority.URGENT); // 8h window

        wo.setSlaDueAt(Instant.now().plusSeconds(10 * 3600)); // far out
        assertEquals(SlaStatus.ON_TRACK, sla.evaluate(wo));

        wo.setSlaDueAt(Instant.now().plusSeconds(60 * 60)); // 1h left of 8h window -> at risk
        assertEquals(SlaStatus.AT_RISK, sla.evaluate(wo));

        wo.setSlaDueAt(Instant.now().minusSeconds(60)); // overdue
        assertEquals(SlaStatus.BREACHED, sla.evaluate(wo));
    }
}