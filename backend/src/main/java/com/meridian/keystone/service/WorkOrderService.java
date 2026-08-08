package com.meridian.keystone.service;

import com.meridian.keystone.domain.Customer;
import com.meridian.keystone.domain.Role;
import com.meridian.keystone.domain.SlaStatus;
import com.meridian.keystone.domain.Site;
import com.meridian.keystone.domain.User;
import com.meridian.keystone.domain.WorkOrder;
import com.meridian.keystone.domain.WorkOrderStatus;
import com.meridian.keystone.domain.WorkOrderStatusHistory;
import com.meridian.keystone.dto.AssignRequest;
import com.meridian.keystone.dto.PagedResponse;
import com.meridian.keystone.dto.PartUsageDto;
import com.meridian.keystone.dto.StatusHistoryDto;
import com.meridian.keystone.dto.StatusRequest;
import com.meridian.keystone.dto.TimeLogDto;
import com.meridian.keystone.dto.WorkOrderDto;
import com.meridian.keystone.dto.WorkOrderRequest;
import com.meridian.keystone.exception.ApiException;
import com.meridian.keystone.repository.CustomerRepository;
import com.meridian.keystone.repository.PartUsageRepository;
import com.meridian.keystone.repository.SiteRepository;
import com.meridian.keystone.repository.TimeLogRepository;
import com.meridian.keystone.repository.UserRepository;
import com.meridian.keystone.repository.WorkOrderRepository;
import com.meridian.keystone.repository.WorkOrderStatusHistoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderStatusHistoryRepository historyRepository;
    private final CustomerRepository customerRepository;
    private final SiteRepository siteRepository;
    private final UserRepository userRepository;
    private final PartUsageRepository partUsageRepository;
    private final TimeLogRepository timeLogRepository;
    private final NotificationService notificationService;
    private final SlaService slaService;

    public WorkOrderService(WorkOrderRepository workOrderRepository,
                            WorkOrderStatusHistoryRepository historyRepository,
                            CustomerRepository customerRepository,
                            SiteRepository siteRepository,
                            UserRepository userRepository,
                            PartUsageRepository partUsageRepository,
                            TimeLogRepository timeLogRepository,
                            NotificationService notificationService,
                            SlaService slaService) {
        this.workOrderRepository = workOrderRepository;
        this.historyRepository = historyRepository;
        this.customerRepository = customerRepository;
        this.siteRepository = siteRepository;
        this.userRepository = userRepository;
        this.partUsageRepository = partUsageRepository;
        this.timeLogRepository = timeLogRepository;
        this.notificationService = notificationService;
        this.slaService = slaService;
    }

    // ------------------------------------------------------------------ CRUD

    @Transactional
    public WorkOrderDto create(WorkOrderRequest request, User actor) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Customer not found"));
        Site site = siteRepository.findById(request.siteId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Site not found"));
        if (!site.getCustomer().getId().equals(customer.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Site does not belong to the selected customer");
        }
        // Customers may only raise requests for their own organisation.
        if (actor.getRole() == Role.CUSTOMER && !actor.getCustomer().getId().equals(customer.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only raise requests for your own sites");
        }

        WorkOrder wo = new WorkOrder();
        wo.setCode(nextCode());
        wo.setTitle(request.title().trim());
        wo.setDescription(request.description());
        wo.setPriority(request.priority());
        wo.setStatus(WorkOrderStatus.NEW);
        wo.setCustomer(customer);
        wo.setSite(site);
        wo.setSlaDueAt(Instant.now().plus(request.priority().getSlaWindow()));
        workOrderRepository.save(wo);
        wo.setSlaStatus(slaService.evaluate(wo));
        workOrderRepository.save(wo);

        recordTransition(wo, null, WorkOrderStatus.NEW, actor, "Work order created");
        return toDto(wo);
    }

    @Transactional(readOnly = true)
    public List<WorkOrderDto> list(User actor) {
        return workOrderRepository.findAll(buildScope(actor)).stream()
                .map(this::toListItem)
                .toList();
    }

    @Transactional(readOnly = true)
    public PagedResponse<WorkOrderDto> list(WorkOrderStatus status, String search, Long customerId,
                                            Long siteId, Long assigneeId, Pageable pageable, User actor) {
        Specification<WorkOrder> spec = buildScope(actor);
        if (status != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), status));
        }
        if (customerId != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("customer").get("id"), customerId));
        }
        if (siteId != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("site").get("id"), siteId));
        }
        if (search != null && !search.isBlank()) {
            String like = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), like),
                    cb.like(cb.lower(root.get("code")), like)));
        }
        Page<WorkOrder> page = workOrderRepository.findAll(spec, pageable);
        return PagedResponse.from(page, this::toListItem);
    }

    @Transactional(readOnly = true)
    public WorkOrderDto get(Long id, User actor) {
        WorkOrder wo = loadScoped(id, actor);
        return toDto(wo);
    }

    @Transactional
    public WorkOrderDto update(Long id, WorkOrderRequest request, User actor) {
        WorkOrder wo = loadScoped(id, actor);
        requireEditable(wo);
        if (wo.getCustomer().getId() != null && !wo.getCustomer().getId().equals(request.customerId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Customer cannot be changed after creation");
        }
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Customer not found"));
        Site site = siteRepository.findById(request.siteId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Site not found"));
        if (!site.getCustomer().getId().equals(customer.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Site does not belong to the selected customer");
        }
        wo.setTitle(request.title().trim());
        wo.setDescription(request.description());
        wo.setPriority(request.priority());
        wo.setSite(site);
        wo.setUpdatedAt(Instant.now());
        // Recompute SLA due date on priority change (only if still open and not yet breached/overdue).
        workOrderRepository.save(wo);
        return toDto(wo);
    }

    // ------------------------------------------------------------------ Assign

    @Transactional
    public WorkOrderDto assign(Long id, AssignRequest request, User actor) {
        WorkOrder wo = loadScoped(id, actor);
        requireEditable(wo);
        User tech = userRepository.findById(request.technicianId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Technician not found"));
        if (tech.getRole() != Role.TECHNICIAN) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Assignee must be a technician");
        }

        boolean isNew = wo.getStatus() == WorkOrderStatus.NEW;
        wo.setAssignee(tech);
        if (isNew) {
            transition(wo, WorkOrderStatus.ASSIGNED, actor, null);
        } else {
            wo.setUpdatedAt(Instant.now());
            workOrderRepository.save(wo);
            recordReassignment(wo, tech, actor);
        }
        notificationService.notifyAssignee(wo, tech, "You have been assigned work order " + wo.getCode());
        return toDto(wo);
    }

    private void recordReassignment(WorkOrder wo, User tech, User actor) {
        WorkOrderStatusHistory h = new WorkOrderStatusHistory();
        h.setWorkOrder(wo);
        h.setFromStatus(wo.getStatus());
        h.setToStatus(wo.getStatus());
        h.setChangedBy(actor);
        h.setNote("Reassigned to " + tech.getFullName());
        h.setChangedAt(Instant.now());
        historyRepository.save(h);
    }

    // ------------------------------------------------------------------ Status machine

    @Transactional
    public WorkOrderDto transitionStatus(Long id, StatusRequest request, User actor) {
        WorkOrder wo = loadScoped(id, actor);
        WorkOrderStatus from = wo.getStatus();
        WorkOrderStatus to = request.status();
        if (from == to) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Work order is already in status " + from);
        }
        validateTransition(wo, to, actor);
        transition(wo, to, actor, request.note());
        return toDto(wo);
    }

    private void validateTransition(WorkOrder wo, WorkOrderStatus to, User actor) {
        if (!WorkOrderStateMachine.allows(wo.getStatus(), to)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Illegal transition " + wo.getStatus() + " -> " + to);
        }
        if (WorkOrderStateMachine.isManagerAction(to) && actor.getRole() != Role.MANAGER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only a manager can close a work order");
        }
        if (WorkOrderStateMachine.isDispatcherOrManagerAction(to)
                && actor.getRole() != Role.MANAGER && actor.getRole() != Role.DISPATCHER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only a dispatcher or manager can cancel a work order");
        }
        if (WorkOrderStateMachine.isTechnicianAction(wo.getStatus(), to)) {
            if (wo.getAssignee() == null || !wo.getAssignee().getId().equals(actor.getId())) {
                throw new ApiException(HttpStatus.FORBIDDEN,
                        "Only the assigned technician can " + actionLabel(to) + " this job");
            }
        }
    }

    private String actionLabel(WorkOrderStatus to) {
        return switch (to) {
            case IN_PROGRESS -> "start or resume";
            case ON_HOLD -> "hold";
            case COMPLETED -> "complete";
            default -> "change";
        };
    }

    private void transition(WorkOrder wo, WorkOrderStatus to, User actor, String note) {
        WorkOrderStatus from = wo.getStatus();
        wo.setStatus(to);
        wo.setUpdatedAt(Instant.now());
        if (to == WorkOrderStatus.COMPLETED) {
            wo.setCompletedAt(Instant.now());
        }
        if (to == WorkOrderStatus.CLOSED || to == WorkOrderStatus.CANCELLED) {
            wo.setSlaStatus(SlaStatus.NOT_APPLICABLE);
        }
        workOrderRepository.save(wo);
        recordTransition(wo, from, to, actor, note);
    }

    private void recordTransition(WorkOrder wo, WorkOrderStatus from, WorkOrderStatus to, User actor, String note) {
        WorkOrderStatusHistory h = new WorkOrderStatusHistory();
        h.setWorkOrder(wo);
        h.setFromStatus(from);
        h.setToStatus(to);
        h.setChangedBy(actor);
        h.setNote(note);
        h.setChangedAt(Instant.now());
        historyRepository.save(h);
        if (to == WorkOrderStatus.COMPLETED) {
            notificationService.workOrderCompleted(wo, actor);
        }
    }

    // ------------------------------------------------------------------ Lookup & helpers

    /** Scope a loaded work order to what the actor may see. */
    public WorkOrder loadScoped(Long id, User actor) {
        WorkOrder wo = workOrderRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Work order not found"));
        if (actor.getRole() == Role.CUSTOMER) {
            if (wo.getCustomer().getId() != null
                    && actor.getCustomer() != null
                    && !wo.getCustomer().getId().equals(actor.getCustomer().getId())) {
                throw new ApiException(HttpStatus.NOT_FOUND, "Work order not found");
            }
        } else if (actor.getRole() == Role.TECHNICIAN) {
            if (wo.getAssignee() == null || !wo.getAssignee().getId().equals(actor.getId())) {
                throw new ApiException(HttpStatus.NOT_FOUND, "Work order not found");
            }
        }
        return wo;
    }

    private void requireEditable(WorkOrder wo) {
        if (wo.getStatus().isTerminal()) {
            throw new ApiException(HttpStatus.CONFLICT, "Work order is " + wo.getStatus() + " and cannot be changed");
        }
    }

    /** Role-scoped specification for list endpoints. */
    private Specification<WorkOrder> buildScope(User actor) {
        if (actor.getRole() == Role.CUSTOMER) {
            Long cid = actor.getCustomer() != null ? actor.getCustomer().getId() : -1L;
            return (root, q, cb) -> cb.equal(root.get("customer").get("id"), cid);
        }
        if (actor.getRole() == Role.TECHNICIAN) {
            return (root, q, cb) -> cb.equal(root.get("assignee").get("id"), actor.getId());
        }
        return Specification.where(null);
    }

    private String nextCode() {
        long n = workOrderRepository.count() + 1;
        String code;
        do {
            code = String.format("WO-%04d", n++);
        } while (workOrderRepository.existsByCode(code));
        return code;
    }

    private WorkOrderDto toListItem(WorkOrder wo) {
        int totalMinutes = timeLogRepository.sumMinutesByWorkOrder(wo.getId());
        BigDecimal totalParts = partUsageRepository.findByWorkOrderId(wo.getId()).stream()
                .map(p -> p.getUnitCost().multiply(BigDecimal.valueOf(p.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return WorkOrderDto.from(wo, List.of(), List.of(), List.of(), totalParts, totalMinutes);
    }

    private WorkOrderDto toDto(WorkOrder wo) {
        List<PartUsageDto> parts = partUsageRepository.findByWorkOrderId(wo.getId()).stream()
                .map(PartUsageDto::from).toList();
        List<TimeLogDto> times = timeLogRepository.findByWorkOrderId(wo.getId()).stream()
                .map(TimeLogDto::from).toList();
        List<StatusHistoryDto> history = historyRepository.findByWorkOrderIdOrderByChangedAtAsc(wo.getId()).stream()
                .map(StatusHistoryDto::from).toList();
        BigDecimal totalPartsCost = parts.stream()
                .map(PartUsageDto::totalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalMinutes = times.stream().mapToInt(TimeLogDto::minutes).sum();
        return WorkOrderDto.from(wo, parts, times, history, totalPartsCost, totalMinutes);
    }
}