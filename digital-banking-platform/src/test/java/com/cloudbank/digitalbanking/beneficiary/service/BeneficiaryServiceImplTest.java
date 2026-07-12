package com.cloudbank.digitalbanking.beneficiary.service;

import com.cloudbank.digitalbanking.beneficiary.dto.BeneficiaryRequest;
import com.cloudbank.digitalbanking.beneficiary.dto.BeneficiaryResponse;
import com.cloudbank.digitalbanking.beneficiary.entity.Beneficiary;
import com.cloudbank.digitalbanking.beneficiary.repository.BeneficiaryRepository;
import com.cloudbank.digitalbanking.customer.repository.CustomerRepository;
import com.cloudbank.digitalbanking.exception.DuplicateResourceException;
import com.cloudbank.digitalbanking.notification.service.NotificationService;
import com.cloudbank.digitalbanking.audit.service.AuditService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BeneficiaryServiceImplTest {

    @Mock
    private BeneficiaryRepository beneficiaryRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private BeneficiaryServiceImpl beneficiaryService;

    @Test
    void createBeneficiary_shouldThrowDuplicateResourceException_whenDuplicateAccountNumberExists() {
        UUID customerId = UUID.randomUUID();
        BeneficiaryRequest request = buildValidRequest(customerId);

        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(beneficiaryRepository.existsByCustomerIdAndBeneficiaryAccountNumber(
                customerId, request.getBeneficiaryAccountNumber())).thenReturn(true);

        assertThatThrownBy(() -> beneficiaryService.createBeneficiary(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

        verify(beneficiaryRepository, never()).save(any(Beneficiary.class));
    }

    @Test
    void createBeneficiary_shouldSaveBeneficiary_whenCustomerExistsAndAccountNumberIsUnique() {
        UUID customerId = UUID.randomUUID();
        BeneficiaryRequest request = buildValidRequest(customerId);
        Beneficiary savedBeneficiary = new Beneficiary();
        savedBeneficiary.setId(UUID.randomUUID());
        savedBeneficiary.setCustomerId(customerId);
        savedBeneficiary.setBeneficiaryName("John Smith");
        savedBeneficiary.setBeneficiaryAccountNumber(request.getBeneficiaryAccountNumber());

        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(beneficiaryRepository.existsByCustomerIdAndBeneficiaryAccountNumber(
                customerId, request.getBeneficiaryAccountNumber())).thenReturn(false);
        when(beneficiaryRepository.save(any(Beneficiary.class))).thenReturn(savedBeneficiary);

        BeneficiaryResponse response = beneficiaryService.createBeneficiary(request);

        org.assertj.core.api.Assertions.assertThat(response.getCustomerId()).isEqualTo(customerId);
        verify(beneficiaryRepository).save(any(Beneficiary.class));
        verify(notificationService).notifyBeneficiaryAdded(customerId, "John Smith");
        verify(auditService).recordEvent(
                org.mockito.ArgumentMatchers.eq(customerId.toString()),
                org.mockito.ArgumentMatchers.eq(com.cloudbank.digitalbanking.audit.enums.AuditAction.BENEFICIARY_CREATED),
                org.mockito.ArgumentMatchers.eq(com.cloudbank.digitalbanking.audit.constants.AuditResourceType.BENEFICIARY),
                org.mockito.ArgumentMatchers.any(String.class),
                org.mockito.ArgumentMatchers.contains("created"),
                org.mockito.ArgumentMatchers.eq(com.cloudbank.digitalbanking.audit.enums.AuditResult.SUCCESS));
    }

    private BeneficiaryRequest buildValidRequest(UUID customerId) {
        BeneficiaryRequest request = new BeneficiaryRequest();
        request.setCustomerId(customerId);
        request.setBeneficiaryName("John Smith");
        request.setBeneficiaryAccountNumber("1234567");
        request.setBankName("CloudBank");
        request.setTransitNumber("12345");
        request.setInstitutionNumber("001");
        request.setNickname("John");
        return request;
    }
}
