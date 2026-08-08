package com.meridian.keystone.controller;

import com.meridian.keystone.domain.User;
import com.meridian.keystone.dto.NotificationDto;
import com.meridian.keystone.dto.PagedResponse;
import com.meridian.keystone.security.CurrentUser;
import com.meridian.keystone.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "In-app notifications for the current user")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "List the current user's notifications")
    public PagedResponse<NotificationDto> list(@PageableDefault(size = 20) Pageable pageable,
                                               @CurrentUser User actor) {
        return notificationService.listForUser(actor.getId(), pageable);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Unread notification count for the current user")
    public long unreadCount(@CurrentUser User actor) {
        return notificationService.unreadCount(actor.getId());
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Mark one notification as read")
    public void markRead(@PathVariable Long id, @CurrentUser User actor) {
        notificationService.markRead(id, actor.getId());
    }

    @PostMapping("/read-all")
    @Operation(summary = "Mark all of the current user's notifications as read")
    public void markAllRead(@CurrentUser User actor) {
        notificationService.markAllRead(actor.getId());
    }
}