package com.meridian.keystone.repository;

import com.meridian.keystone.domain.WorkOrder;
import com.meridian.keystone.domain.WorkOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long>, JpaSpecificationExecutor<WorkOrder> {

    boolean existsByCode(String code);

    List<WorkOrder> findByStatusNotIn(List<WorkOrderStatus> statuses);
    @Query("""
            select wo.status, count(wo)
            from WorkOrder wo
            where (:customerId is null or wo.customer.id = :customerId)
              and (:assigneeId is null or wo.assignee.id = :assigneeId)
            group by wo.status
            """)
    List<Object[]> countByStatusGrouped(
            @Param("customerId") Long customerId,
            @Param("assigneeId") Long assigneeId);

    @Query("""
            select wo.slaStatus, count(wo)
            from WorkOrder wo
            where (:customerId is null or wo.customer.id = :customerId)
              and (:assigneeId is null or wo.assignee.id = :assigneeId)
            group by wo.slaStatus
            """)
    List<Object[]> countBySlaStatusGrouped(
            @Param("customerId") Long customerId,
            @Param("assigneeId") Long assigneeId);

    @Query("""
            select wo.slaStatus, count(wo)
            from WorkOrder wo
            where wo.status not in ('CLOSED','CANCELLED')
              and (:customerId is null or wo.customer.id = :customerId)
              and (:assigneeId is null or wo.assignee.id = :assigneeId)
            group by wo.slaStatus
            """)
    List<Object[]> countOpenSlaStatusGrouped(
            @Param("customerId") Long customerId,
            @Param("assigneeId") Long assigneeId);

    /** Overdue = open (not closed/cancelled) and not yet completed, with due date in the past. */
    @Query("""
            select count(wo) from WorkOrder wo
            where wo.slaDueAt is not null
              and wo.slaDueAt < current_timestamp
              and wo.status not in ('CLOSED','CANCELLED')
              and (:customerId is null or wo.customer.id = :customerId)
              and (:assigneeId is null or wo.assignee.id = :assigneeId)
            """)
    long countOverdue(
            @Param("customerId") Long customerId,
            @Param("assigneeId") Long assigneeId);

    @Query("""
            select u.id, u.fullName, count(wo)
            from WorkOrder wo join wo.assignee u
            where (:customerId is null or wo.customer.id = :customerId)
              and (:assigneeId is null or wo.assignee.id = :assigneeId)
            group by u.id, u.fullName
            order by count(wo) desc
            """)
    List<Object[]> countByTechnician(
            @Param("customerId") Long customerId,
            @Param("assigneeId") Long assigneeId);

    @Query("""
            select c.name, count(wo)
            from WorkOrder wo join wo.customer c
            where (:customerId is null or c.id = :customerId)
              and (:assigneeId is null or wo.assignee.id = :assigneeId)
            group by c.name
            order by count(wo) desc
            """)
    List<Object[]> countByCustomer(
            @Param("customerId") Long customerId,
            @Param("assigneeId") Long assigneeId);

    @Query("""
            select s.name, count(wo)
            from WorkOrder wo join wo.site s
            where (:customerId is null or wo.customer.id = :customerId)
              and (:assigneeId is null or wo.assignee.id = :assigneeId)
            group by s.name
            order by count(wo) desc
            """)
    List<Object[]> countBySite(
            @Param("customerId") Long customerId,
            @Param("assigneeId") Long assigneeId);

    /** Terminal, non-cancelled orders that were completed on-or-before their SLA due. */
    @Query("""
            select count(wo) from WorkOrder wo
            where wo.status in ('COMPLETED','CLOSED')
              and wo.slaDueAt is not null
              and wo.completedAt is not null
              and wo.completedAt <= wo.slaDueAt
              and (:customerId is null or wo.customer.id = :customerId)
              and (:assigneeId is null or wo.assignee.id = :assigneeId)
            """)
    long countCompletedOnTime(
            @Param("customerId") Long customerId,
            @Param("assigneeId") Long assigneeId);

    /** All terminal non-cancelled orders that had an SLA due date. */
    @Query("""
            select count(wo) from WorkOrder wo
            where wo.status in ('COMPLETED','CLOSED')
              and wo.slaDueAt is not null
              and (:customerId is null or wo.customer.id = :customerId)
              and (:assigneeId is null or wo.assignee.id = :assigneeId)
            """)
    long countTerminalWithSla(
            @Param("customerId") Long customerId,
            @Param("assigneeId") Long assigneeId);
}