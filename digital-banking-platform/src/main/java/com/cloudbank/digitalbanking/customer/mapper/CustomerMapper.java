package com.cloudbank.digitalbanking.customer.mapper;

import com.cloudbank.digitalbanking.common.constants.BankingConstants;
import com.cloudbank.digitalbanking.common.util.ReferenceGenerator;
import com.cloudbank.digitalbanking.customer.dto.CustomerRequest;
import com.cloudbank.digitalbanking.customer.dto.CustomerResponse;
import com.cloudbank.digitalbanking.customer.entity.Customer;
import com.cloudbank.digitalbanking.customer.enums.CustomerStatus;

public final class CustomerMapper {

    private CustomerMapper() {
    }

    public static Customer toEntity(CustomerRequest request) {
        Customer customer = new Customer();
        customer.setCustomerNumber(ReferenceGenerator.generateCustomerId());
        applyRequest(customer, request);
        customer.setStatus(CustomerStatus.ACTIVE);
        return customer;
    }

    public static void updateEntity(Customer customer, CustomerRequest request) {
        applyRequest(customer, request);
    }

    public static CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .customerNumber(customer.getCustomerNumber())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .dateOfBirth(customer.getDateOfBirth())
                .addressLine1(customer.getAddressLine1())
                .city(customer.getCity())
                .province(customer.getProvince())
                .postalCode(customer.getPostalCode())
                .country(customer.getCountry())
                .status(customer.getStatus())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    private static void applyRequest(Customer customer, CustomerRequest request) {
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setDateOfBirth(request.getDateOfBirth());
        customer.setAddressLine1(request.getAddressLine1());
        customer.setCity(request.getCity());
        customer.setProvince(request.getProvince());
        customer.setPostalCode(normalizePostalCode(request.getPostalCode()));
        customer.setCountry(resolveCountry(request.getCountry()));
    }

    private static String resolveCountry(String country) {
        if (country == null || country.isBlank()) {
            return BankingConstants.DEFAULT_COUNTRY;
        }
        return country.trim();
    }

    private static String normalizePostalCode(String postalCode) {
        return postalCode.replace(" ", "").toUpperCase();
    }
}
