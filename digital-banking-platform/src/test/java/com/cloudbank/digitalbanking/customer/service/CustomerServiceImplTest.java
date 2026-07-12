package com.cloudbank.digitalbanking.customer.service;

import com.cloudbank.digitalbanking.audit.service.AuditService;
import com.cloudbank.digitalbanking.customer.dto.CustomerRequest;
import com.cloudbank.digitalbanking.customer.dto.CustomerResponse;
import com.cloudbank.digitalbanking.customer.entity.Customer;
import com.cloudbank.digitalbanking.customer.exception.CustomerNotFoundException;
import com.cloudbank.digitalbanking.customer.repository.CustomerRepository;
import com.cloudbank.digitalbanking.exception.DuplicateResourceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @Test
    void createCustomer_shouldReturnCustomerResponse_whenEmailIsUnique() {
        CustomerRequest request = buildValidRequest();

        Customer savedCustomer = new Customer();
        savedCustomer.setId(UUID.randomUUID());
        savedCustomer.setFirstName(request.getFirstName());
        savedCustomer.setLastName(request.getLastName());
        savedCustomer.setEmail(request.getEmail());
        savedCustomer.setPhoneNumber(request.getPhoneNumber());

        when(customerRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        CustomerResponse response = customerService.createCustomer(request);

        assertThat(response.getFirstName()).isEqualTo("Jane");
        assertThat(response.getLastName()).isEqualTo("Doe");
        assertThat(response.getEmail()).isEqualTo("jane.doe@example.com");
        verify(customerRepository).save(any(Customer.class));
        verify(auditService).recordEvent(
                org.mockito.ArgumentMatchers.any(String.class),
                org.mockito.ArgumentMatchers.eq(com.cloudbank.digitalbanking.audit.enums.AuditAction.CUSTOMER_CREATED),
                org.mockito.ArgumentMatchers.eq(com.cloudbank.digitalbanking.audit.constants.AuditResourceType.CUSTOMER),
                org.mockito.ArgumentMatchers.any(String.class),
                org.mockito.ArgumentMatchers.contains("Customer created"),
                org.mockito.ArgumentMatchers.eq(com.cloudbank.digitalbanking.audit.enums.AuditResult.SUCCESS));
    }

    @Test
    void createCustomer_shouldThrowDuplicateResourceException_whenEmailExists() {
        CustomerRequest request = buildValidRequest();
        when(customerRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> customerService.createCustomer(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void getCustomerById_shouldThrowCustomerNotFoundException_whenCustomerDoesNotExist() {
        UUID customerId = UUID.randomUUID();
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getCustomerById(customerId))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining(customerId.toString());
    }

    private CustomerRequest buildValidRequest() {
        CustomerRequest request = new CustomerRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setEmail("jane.doe@example.com");
        request.setPhoneNumber("+14165551234");
        request.setDateOfBirth(LocalDate.of(1990, 5, 15));
        request.setAddressLine1("123 Main Street");
        request.setCity("Toronto");
        request.setProvince("Ontario");
        request.setPostalCode("M5V 2T6");
        return request;
    }
}
