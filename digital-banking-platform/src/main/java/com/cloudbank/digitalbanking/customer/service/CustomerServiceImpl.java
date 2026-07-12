package com.cloudbank.digitalbanking.customer.service;

import com.cloudbank.digitalbanking.audit.constants.AuditResourceType;
import com.cloudbank.digitalbanking.audit.enums.AuditAction;
import com.cloudbank.digitalbanking.audit.enums.AuditResult;
import com.cloudbank.digitalbanking.audit.service.AuditService;
import com.cloudbank.digitalbanking.customer.dto.CustomerRequest;
import com.cloudbank.digitalbanking.customer.dto.CustomerResponse;
import com.cloudbank.digitalbanking.customer.entity.Customer;
import com.cloudbank.digitalbanking.customer.enums.CustomerStatus;
import com.cloudbank.digitalbanking.customer.exception.CustomerNotFoundException;
import com.cloudbank.digitalbanking.customer.mapper.CustomerMapper;
import com.cloudbank.digitalbanking.customer.repository.CustomerRepository;
import com.cloudbank.digitalbanking.exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        validateUniqueEmail(request.getEmail(), null);
        Customer customer = customerRepository.save(CustomerMapper.toEntity(request));
        auditService.recordEvent(
                customer.getId().toString(),
                AuditAction.CUSTOMER_CREATED,
                AuditResourceType.CUSTOMER,
                customer.getId().toString(),
                "Customer created with number " + customer.getCustomerNumber(),
                AuditResult.SUCCESS);
        return CustomerMapper.toResponse(customer);
    }

    @Override
    public CustomerResponse getCustomerById(UUID id) {
        Customer customer = findCustomerOrThrow(id);
        return CustomerMapper.toResponse(customer);
    }

    @Override
    public CustomerResponse getCustomerByCustomerNumber(String customerNumber) {
        Customer customer = customerRepository.findByCustomerNumber(customerNumber)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found with customer number: " + customerNumber));
        return CustomerMapper.toResponse(customer);
    }

    @Override
    public CustomerResponse getCustomerByEmail(String email) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found with email: " + email));
        return CustomerMapper.toResponse(customer);
    }

    @Override
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(CustomerMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(UUID id, CustomerRequest request) {
        Customer customer = findCustomerOrThrow(id);
        validateUniqueEmail(request.getEmail(), id);
        CustomerMapper.updateEntity(customer, request);
        auditService.recordEvent(
                customer.getId().toString(),
                AuditAction.CUSTOMER_PROFILE_UPDATED,
                AuditResourceType.CUSTOMER,
                customer.getId().toString(),
                "Customer profile updated for customer number " + customer.getCustomerNumber(),
                AuditResult.SUCCESS);
        return CustomerMapper.toResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomerStatus(UUID id, CustomerStatus status) {
        Customer customer = findCustomerOrThrow(id);
        customer.setStatus(status);
        return CustomerMapper.toResponse(customer);
    }

    private Customer findCustomerOrThrow(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));
    }

    private void validateUniqueEmail(String email, UUID excludeId) {
        boolean emailExists = excludeId == null
                ? customerRepository.existsByEmail(email)
                : customerRepository.existsByEmailAndIdNot(email, excludeId);

        if (emailExists) {
            throw new DuplicateResourceException("Customer with email already exists: " + email);
        }
    }
}
