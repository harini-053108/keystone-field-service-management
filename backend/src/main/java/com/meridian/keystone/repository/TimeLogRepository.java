package com.meridian.keystone.repository;

import com.meridian.keystone.domain.TimeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TimeLogRepository extends JpaRepository<TimeLog, Long> {

    List<TimeLog> findByWorkOrderId(Long workOrderId);

    @Query("select coalesce(sum(t.minutes), 0) from TimeLog t where t.workOrder.id = :workOrderId")
    int sumMinutesByWorkOrder(@Param("workOrderId") Long workOrderId);
}