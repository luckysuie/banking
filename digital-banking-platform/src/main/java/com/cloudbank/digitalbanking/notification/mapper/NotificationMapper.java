package com.cloudbank.digitalbanking.notification.mapper;

import com.cloudbank.digitalbanking.notification.dto.NotificationResponse;
import com.cloudbank.digitalbanking.notification.entity.Notification;
import com.cloudbank.digitalbanking.notification.enums.NotificationStatus;
import com.cloudbank.digitalbanking.notification.enums.NotificationType;

import java.util.UUID;

public final class NotificationMapper {

    private NotificationMapper() {
    }

    public static Notification toEntity(UUID customerId, NotificationType type, String message) {
        Notification notification = new Notification();
        notification.setCustomerId(customerId);
        notification.setType(type);
        notification.setMessage(message);
        notification.setStatus(NotificationStatus.CREATED);
        return notification;
    }

    public static NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .customerId(notification.getCustomerId())
                .type(notification.getType())
                .message(notification.getMessage())
                .status(notification.getStatus())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
