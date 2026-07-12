package com.cloudbank.digitalbanking.customer.service;

import com.cloudbank.digitalbanking.customer.dto.CustomerRequest;
import com.cloudbank.digitalbanking.customer.dto.CustomerResponse;
import com.cloudbank.digitalbanking.customer.enums.CustomerStatus;

import java.util.List;
import java.util.UUID;

public interface CustomerService {

    CustomerResponse createCustomer(CustomerRequest request);

    CustomerResponse getCustomerById(UUID id);

    CustomerResponse getCustomerByCustomerNumber(String customerNumber);

    CustomerResponse getCustomerByEmail(String email);

    List<CustomerResponse> getAllCustomers();

    CustomerResponse updateCustomer(UUID id, CustomerRequest request);

    CustomerResponse updateCustomerStatus(UUID id, CustomerStatus status);
}
