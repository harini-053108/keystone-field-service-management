package com.meridian.keystone.controller;

import com.meridian.keystone.domain.Role;
import com.meridian.keystone.domain.User;
import com.meridian.keystone.domain.WorkOrderStatus;
import com.meridian.keystone.dto.AssignRequest;
import com.meridian.keystone.dto.PagedResponse;
import com.meridian.keystone.dto.PartUsageDto;
import com.meridian.keystone.dto.PartUsageRequest;
import com.meridian.keystone.dto.StatusRequest;
import com.meridian.keystone.dto.TimeLogDto;
import com.meridian.keystone.dto.TimeLogRequest;
import com.meridian.keystone.dto.WorkOrderDto;
import com.meridian.keystone.dto.WorkOrderRequest;
import com.meridian.keystone.exception.ApiException;
import com.meridian.keystone.security.CurrentUser;
import com.meridian.keystone.service.PartUsageService;
import com.meridian.keystone.service.TimeLogService;
import com.meridian.keystone.service.WorkOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/work-orders")
@Tag(name = "Work Orders", description = "The core field-service object with its lifecycle")
public class WorkOrderController {

    private final WorkOrderService workOrderService;
    private final PartUsageService partUsageService;
    private final TimeLogService timeLogService;

    public WorkOrderController(WorkOrderService workOrderService, PartUsageService partUsageService,
                               TimeLogService timeLogService) {
        this.workOrderService = workOrderService;
        this.partUsageService = partUsageService;
        this.timeLogService = timeLogService;
    }

    @GetMapping
    @Operation(summary = "List work orders (role-scoped, filterable, paginated)")
    public PagedResponse<WorkOrderDto> list(@RequestParam(required = false) WorkOrderStatus status,
                                            @RequestParam(required = false) String search,
                                            @RequestParam(required = false) Long customerId,
                                            @RequestParam(required = false) Long siteId,
                                            @RequestParam(required = false) Long assigneeId,
                                            @PageableDefault(size = 20) Pageable pageable,
                                            @CurrentUser User actor) {
        return workOrderService.list(status, search, customerId, siteId, assigneeId, pageable, actor);
    }

    @GetMapping("/board")
    @PreAuthorize("hasAnyRole('DISPATCHER','MANAGER')")
    @Operation(summary = "All open work orders for the Kanban board")
    public List<WorkOrderDto> board(@CurrentUser User actor) {
        return workOrderService.list(actor).stream()
                .filter(dto -> !dto.status().isTerminal())
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a work order with history, parts and time")
    public WorkOrderDto get(@PathVariable Long id, @CurrentUser User actor) {
        return workOrderService.get(id, actor);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DISPATCHER','MANAGER','CUSTOMER')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a work order (or raise a customer request)")
    public WorkOrderDto create(@Valid @RequestBody WorkOrderRequest request, @CurrentUser User actor) {
        return workOrderService.create(request, actor);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DISPATCHER','MANAGER')")
    @Operation(summary = "Update an open work order")
    public WorkOrderDto update(@PathVariable Long id, @Valid @RequestBody WorkOrderRequest request,
                               @CurrentUser User actor) {
        return workOrderService.update(id, request, actor);
    }

    @PostMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('DISPATCHER','MANAGER')")
    @Operation(summary = "Assign (or reassign) a technician")
    public WorkOrderDto assign(@PathVariable Long id, @Valid @RequestBody AssignRequest request,
                               @CurrentUser User actor) {
        return workOrderService.assign(id, request, actor);
    }

    @PostMapping("/{id}/status")
    @Operation(summary = "Transition status (validated against the lifecycle; 409 on illegal)")
    public WorkOrderDto transition(@PathVariable Long id, @Valid @RequestBody StatusRequest request,
                                   @CurrentUser User actor) {
        return workOrderService.transitionStatus(id, request, actor);
    }

    @PostMapping("/{id}/parts")
    @PreAuthorize("hasAnyRole('DISPATCHER','MANAGER','TECHNICIAN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Log parts used (decrements stock in the same transaction)")
    public PartUsageDto logPart(@PathVariable Long id, @Valid @RequestBody PartUsageRequest request,
                                @CurrentUser User actor) {
        return partUsageService.log(id, request, actor);
    }

    @PostMapping("/{id}/time")
    @PreAuthorize("hasAnyRole('DISPATCHER','MANAGER','TECHNICIAN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Log labour time against a work order")
    public TimeLogDto logTime(@PathVariable Long id, @Valid @RequestBody TimeLogRequest request,
                              @CurrentUser User actor) {
        return timeLogService.log(id, request, actor);
    }
}