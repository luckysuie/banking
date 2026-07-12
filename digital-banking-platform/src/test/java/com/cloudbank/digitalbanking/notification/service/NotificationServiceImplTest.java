package com.cloudbank.digitalbanking.notification.service;

import com.cloudbank.digitalbanking.notification.entity.Notification;
import com.cloudbank.digitalbanking.notification.enums.NotificationStatus;
import com.cloudbank.digitalbanking.notification.enums.NotificationType;
import com.cloudbank.digitalbanking.notification.repository.NotificationRepository;
import com.cloudbank.digitalbanking.customer.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void notifyPaymentSuccess_shouldPersistPaymentSuccessNotification() {
        UUID customerId = UUID.randomUUID();
        Notification saved = new Notification();
        saved.setCustomerId(customerId);
        saved.setType(NotificationType.PAYMENT_SUCCESS);
        saved.setMessage("Payment PAY-TEST123 of 100.00 CAD completed successfully.");

        when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

        notificationService.notifyPaymentSuccess(customerId, "PAY-TEST123",
                new java.math.BigDecimal("100.00"), "CAD");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues().getFirst().getType()).isEqualTo(NotificationType.PAYMENT_SUCCESS);
        assertThat(captor.getAllValues().getLast().getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void notifyAccountCreated_shouldPersistAndMarkAsSent() {
        UUID customerId = UUID.randomUUID();
        Notification saved = new Notification();
        saved.setCustomerId(customerId);
        saved.setType(NotificationType.ACCOUNT_CREATED);
        saved.setMessage("Your new account CB1234567890 has been created successfully.");

        when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

        notificationService.notifyAccountCreated(customerId, "CB1234567890");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues().getLast().getStatus()).isEqualTo(NotificationStatus.SENT);
    }
}
