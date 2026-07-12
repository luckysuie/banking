package com.cloudbank.digitalbanking.beneficiary.service;

import com.cloudbank.digitalbanking.beneficiary.dto.BeneficiaryRequest;
import com.cloudbank.digitalbanking.beneficiary.dto.BeneficiaryResponse;
import com.cloudbank.digitalbanking.beneficiary.dto.BeneficiaryUpdateRequest;
import com.cloudbank.digitalbanking.beneficiary.entity.Beneficiary;
import com.cloudbank.digitalbanking.beneficiary.enums.BeneficiaryStatus;
import com.cloudbank.digitalbanking.beneficiary.exception.BeneficiaryNotFoundException;
import com.cloudbank.digitalbanking.beneficiary.mapper.BeneficiaryMapper;
import com.cloudbank.digitalbanking.beneficiary.repository.BeneficiaryRepository;
import com.cloudbank.digitalbanking.audit.constants.AuditResourceType;
import com.cloudbank.digitalbanking.audit.enums.AuditAction;
import com.cloudbank.digitalbanking.audit.enums.AuditResult;
import com.cloudbank.digitalbanking.audit.service.AuditService;
import com.cloudbank.digitalbanking.customer.exception.CustomerNotFoundException;
import com.cloudbank.digitalbanking.customer.repository.CustomerRepository;
import com.cloudbank.digitalbanking.exception.DuplicateResourceException;
import com.cloudbank.digitalbanking.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BeneficiaryServiceImpl implements BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final CustomerRepository customerRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    @Override
    @Transactional
    public BeneficiaryResponse createBeneficiary(BeneficiaryRequest request) {
        validateCustomerExists(request.getCustomerId());
        validateUniqueAccountNumber(
                request.getCustomerId(),
                request.getBeneficiaryAccountNumber(),
                null);

        Beneficiary beneficiary = beneficiaryRepository.save(BeneficiaryMapper.toEntity(request));
        notificationService.notifyBeneficiaryAdded(
                request.getCustomerId(), beneficiary.getBeneficiaryName());
        auditService.recordEvent(
                request.getCustomerId().toString(),
                AuditAction.BENEFICIARY_CREATED,
                AuditResourceType.BENEFICIARY,
                beneficiary.getId().toString(),
                "Beneficiary '" + beneficiary.getBeneficiaryName() + "' created",
                AuditResult.SUCCESS);
        return BeneficiaryMapper.toResponse(beneficiary);
    }

    @Override
    public BeneficiaryResponse getBeneficiaryById(UUID id) {
        return BeneficiaryMapper.toResponse(findBeneficiaryOrThrow(id));
    }

    @Override
    public List<BeneficiaryResponse> getBeneficiariesByCustomerId(UUID customerId) {
        validateCustomerExists(customerId);
        return beneficiaryRepository.findByCustomerId(customerId).stream()
                .map(BeneficiaryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public BeneficiaryResponse updateBeneficiary(UUID id, BeneficiaryUpdateRequest request) {
        Beneficiary beneficiary = findBeneficiaryOrThrow(id);
        validateUniqueAccountNumber(
                beneficiary.getCustomerId(),
                request.getBeneficiaryAccountNumber(),
                id);

        BeneficiaryMapper.updateEntity(beneficiary, request);
        return BeneficiaryMapper.toResponse(beneficiary);
    }

    @Override
    @Transactional
    public BeneficiaryResponse updateBeneficiaryStatus(UUID id, BeneficiaryStatus status) {
        Beneficiary beneficiary = findBeneficiaryOrThrow(id);
        beneficiary.setStatus(status);
        return BeneficiaryMapper.toResponse(beneficiary);
    }

    @Override
    @Transactional
    public void deleteBeneficiary(UUID id) {
        Beneficiary beneficiary = findBeneficiaryOrThrow(id);
        beneficiaryRepository.delete(beneficiary);
    }

    private Beneficiary findBeneficiaryOrThrow(UUID id) {
        return beneficiaryRepository.findById(id)
                .orElseThrow(() -> new BeneficiaryNotFoundException("Beneficiary not found with id: " + id));
    }

    private void validateCustomerExists(UUID customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException("Customer not found with id: " + customerId);
        }
    }

    private void validateUniqueAccountNumber(UUID customerId, String accountNumber, UUID excludeId) {
        boolean duplicate = excludeId == null
                ? beneficiaryRepository.existsByCustomerIdAndBeneficiaryAccountNumber(customerId, accountNumber)
                : beneficiaryRepository.existsByCustomerIdAndBeneficiaryAccountNumberAndIdNot(
                        customerId, accountNumber, excludeId);

        if (duplicate) {
            throw new DuplicateResourceException(
                    "Beneficiary account number already exists for this customer: " + accountNumber);
        }
    }
}
