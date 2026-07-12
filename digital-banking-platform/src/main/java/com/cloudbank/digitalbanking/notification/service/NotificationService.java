package com.cloudbank.digitalbanking.notification.service;

import com.cloudbank.digitalbanking.notification.dto.NotificationResponse;
import com.cloudbank.digitalbanking.notification.enums.NotificationType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface NotificationService {

    NotificationResponse notifyAccountCreated(UUID customerId, String accountNumber);

    NotificationResponse notifyBeneficiaryAdded(UUID customerId, String beneficiaryName);

    NotificationResponse notifyPaymentSuccess(UUID customerId, String paymentReference, BigDecimal amount, String currency);

    void notifyPaymentFailed(UUID customerId, String reason);

    List<NotificationResponse> getNotificationsByCustomerId(UUID customerId);

    NotificationResponse markAsRead(UUID id);
}
