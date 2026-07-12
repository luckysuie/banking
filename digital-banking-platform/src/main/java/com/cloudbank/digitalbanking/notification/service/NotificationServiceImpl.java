package com.cloudbank.digitalbanking.notification.service;

import com.cloudbank.digitalbanking.customer.exception.CustomerNotFoundException;
import com.cloudbank.digitalbanking.customer.repository.CustomerRepository;
import com.cloudbank.digitalbanking.notification.dto.NotificationResponse;
import com.cloudbank.digitalbanking.notification.entity.Notification;
import com.cloudbank.digitalbanking.notification.enums.NotificationStatus;
import com.cloudbank.digitalbanking.notification.enums.NotificationType;
import com.cloudbank.digitalbanking.notification.exception.NotificationNotFoundException;
import com.cloudbank.digitalbanking.notification.mapper.NotificationMapper;
import com.cloudbank.digitalbanking.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public NotificationResponse notifyAccountCreated(UUID customerId, String accountNumber) {
        String message = "Your new account " + accountNumber + " has been created successfully.";
        return dispatch(customerId, NotificationType.ACCOUNT_CREATED, message);
    }

    @Override
    @Transactional
    public NotificationResponse notifyBeneficiaryAdded(UUID customerId, String beneficiaryName) {
        String message = "Beneficiary '" + beneficiaryName + "' has been added to your profile.";
        return dispatch(customerId, NotificationType.BENEFICIARY_ADDED, message);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NotificationResponse notifyPaymentSuccess(
            UUID customerId,
            String paymentReference,
            BigDecimal amount,
            String currency) {
        String message = "Payment " + paymentReference + " of " + amount + " " + currency
                + " completed successfully.";
        return dispatch(customerId, NotificationType.PAYMENT_SUCCESS, message);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyPaymentFailed(UUID customerId, String reason) {
        String message = "Payment failed: " + reason;
        dispatch(customerId, NotificationType.PAYMENT_FAILED, message);
    }

    @Override
    public List<NotificationResponse> getNotificationsByCustomerId(UUID customerId) {
        validateCustomerExists(customerId);
        return notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(NotificationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with id: " + id));
        notification.setRead(true);
        return NotificationMapper.toResponse(notification);
    }

    private NotificationResponse dispatch(UUID customerId, NotificationType type, String message) {
        Notification notification = notificationRepository.save(
                NotificationMapper.toEntity(customerId, type, message));

        printToConsole(type, message);

        notification.setStatus(NotificationStatus.SENT);
        return NotificationMapper.toResponse(notificationRepository.save(notification));
    }

    private void printToConsole(NotificationType type, String message) {
        System.out.println("[NOTIFICATION][" + type + "] " + message);
    }

    private void validateCustomerExists(UUID customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException("Customer not found with id: " + customerId);
        }
    }
}
