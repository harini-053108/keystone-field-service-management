package com.meridian.keystone.service;

import com.meridian.keystone.domain.Notification;
import com.meridian.keystone.domain.Role;
import com.meridian.keystone.domain.User;
import com.meridian.keystone.domain.WorkOrder;
import com.meridian.keystone.dto.NotificationDto;
import com.meridian.keystone.dto.PagedResponse;
import com.meridian.keystone.exception.ApiException;
import com.meridian.keystone.repository.NotificationRepository;
import com.meridian.keystone.repository.UserRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/** In-app notifications. A notification is persisted (in-app) rather than emailed. */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void notify(User recipient, String message, String type, WorkOrder workOrder) {
        if (recipient == null) {
            return;
        }
        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setMessage(message);
        n.setType(type);
        n.setWorkOrder(workOrder);
        n.setCreatedAt(Instant.now());
        notificationRepository.save(n);
    }

    public void notifyAssignee(WorkOrder wo, User assignee, String message) {
        notify(assignee, message, "ASSIGNMENT", wo);
    }

    public void workOrderCompleted(WorkOrder wo, User actor) {
        // Notify managers + the customer so they can sign off.
        notifyManagers("Work order " + wo.getCode() + " marked COMPLETED by " + actor.getFullName(), wo);
        if (wo.getCustomer() != null) {
            List<User> customers = userRepository.findByRoleAndCustomerId(Role.CUSTOMER, wo.getCustomer().getId());
            for (User c : customers) {
                notify(c, "Work order " + wo.getCode() + " is COMPLETE and awaiting sign-off", "STATUS_CHANGE", wo);
            }
        }
    }

    public void slaBreach(WorkOrder wo) {
        notifyManagers("Work order " + wo.getCode() + " \"" + wo.getTitle() + "\" has breached its SLA", wo);
    }

    public void slaAtRisk(WorkOrder wo) {
        notifyManagers("Work order " + wo.getCode() + " is at risk of breaching its SLA", wo);
    }

    private void notifyManagers(String message, WorkOrder wo) {
        List<User> managers = userRepository.findByRole(Role.MANAGER);
        for (User m : managers) {
            notify(m, message, "SLA_BREACH", wo);
        }
    }

    // --------------------------------------------------------------- queries

    @Transactional(readOnly = true)
    public PagedResponse<NotificationDto> listForUser(Long userId, Pageable pageable) {
        return PagedResponse.from(
                notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable),
                NotificationDto::from);
    }

    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndReadFalse(userId);
    }

    @Transactional
    public void markRead(Long id, Long userId) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Notification not found"));
        if (!n.getRecipient().getId().equals(userId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Notification not found");
        }
        n.setRead(true);
        notificationRepository.save(n);
    }

    @Transactional
    public void markAllRead(Long userId) {
        for (Notification n : notificationRepository.findByRecipientIdAndReadFalse(userId)) {
            n.setRead(true);
            notificationRepository.save(n);
        }
    }
}