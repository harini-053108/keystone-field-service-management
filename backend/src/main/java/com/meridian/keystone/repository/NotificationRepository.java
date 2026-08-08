package com.meridian.keystone.repository;

import com.meridian.keystone.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    List<Notification> findByRecipientIdAndReadFalse(Long recipientId);

    long countByRecipientIdAndReadFalse(Long recipientId);
}