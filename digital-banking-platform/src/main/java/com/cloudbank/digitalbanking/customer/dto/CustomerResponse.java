package com.cloudbank.digitalbanking.customer.dto;

import com.cloudbank.digitalbanking.customer.enums.CustomerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "Customer response payload")
public class CustomerResponse {

    private UUID id;
    private String customerNumber;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String addressLine1;
    private String city;
    private String province;
    private String postalCode;
    private String country;
    private CustomerStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
