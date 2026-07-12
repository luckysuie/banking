package com.cloudbank.digitalbanking.audit.service;

import com.cloudbank.digitalbanking.audit.entity.AuditEvent;
import com.cloudbank.digitalbanking.audit.enums.AuditAction;
import com.cloudbank.digitalbanking.audit.enums.AuditResult;
import com.cloudbank.digitalbanking.audit.repository.AuditRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceImplTest {

    @Mock
    private AuditRepository auditRepository;

    @InjectMocks
    private AuditServiceImpl auditService;

    @Test
    void recordEvent_shouldPersistAuditEvent() {
        when(auditRepository.save(any(AuditEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        auditService.recordEvent(
                "actor-1",
                AuditAction.CUSTOMER_CREATED,
                "CUSTOMER",
                "resource-1",
                "Customer created with number CUS-ABC",
                AuditResult.SUCCESS);

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditRepository).save(captor.capture());
        AuditEvent saved = captor.getValue();
        assertThat(saved.getAction()).isEqualTo(AuditAction.CUSTOMER_CREATED);
        assertThat(saved.getResult()).isEqualTo(AuditResult.SUCCESS);
        assertThat(saved.getEventReference()).startsWith("AUD-");
    }
}
