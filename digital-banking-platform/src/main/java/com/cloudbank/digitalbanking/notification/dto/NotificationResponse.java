package com.cloudbank.digitalbanking.notification.dto;

import com.cloudbank.digitalbanking.notification.enums.NotificationStatus;
import com.cloudbank.digitalbanking.notification.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "Notification response payload")
public class NotificationResponse {

    private UUID id;
    private UUID customerId;
    private NotificationType type;
    private String message;
    private NotificationStatus status;
    private boolean read;
    private Instant createdAt;
}
