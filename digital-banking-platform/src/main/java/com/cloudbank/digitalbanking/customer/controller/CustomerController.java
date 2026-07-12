package com.cloudbank.digitalbanking.customer.controller;

import com.cloudbank.digitalbanking.common.dto.ApiResponse;
import com.cloudbank.digitalbanking.customer.dto.CustomerRequest;
import com.cloudbank.digitalbanking.customer.dto.CustomerResponse;
import com.cloudbank.digitalbanking.customer.dto.CustomerStatusUpdateRequest;
import com.cloudbank.digitalbanking.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Management", description = "APIs for managing bank customers")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @Operation(summary = "Create a new customer", description = "Registers a new customer with a generated customer number")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "Customer created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Validation failed or email already exists")
    })
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Customer found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(
            @Parameter(description = "Customer UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(customerService.getCustomerById(id)));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current customer profile", description = "Resolves the customer from the Entra ID token email claim")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCurrentCustomer(Authentication authentication) {
        String email = resolveAuthenticatedEmail(authentication);
        return ResponseEntity.ok(ApiResponse.success(customerService.getCustomerByEmail(email)));
    }

    @GetMapping("/customer-number/{customerNumber}")
    @Operation(summary = "Get customer by customer number")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Customer found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerByCustomerNumber(
            @Parameter(description = "Unique customer number (e.g. CUS-A1B2C3D4E5F6)")
            @PathVariable String customerNumber) {
        return ResponseEntity.ok(ApiResponse.success(
                customerService.getCustomerByCustomerNumber(customerNumber)));
    }

    @GetMapping
    @Operation(summary = "Get all customers")
    public ResponseEntity<ApiResponse<List<CustomerResponse>>> getAllCustomers() {
        return ResponseEntity.ok(ApiResponse.success(customerService.getAllCustomers()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer details")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Customer updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Validation failed or email already exists"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomer(
            @Parameter(description = "Customer UUID") @PathVariable UUID id,
            @Valid @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update customer status")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Customer status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<ApiResponse<CustomerResponse>> updateCustomerStatus(
            @Parameter(description = "Customer UUID") @PathVariable UUID id,
            @Valid @RequestBody CustomerStatusUpdateRequest request) {
        CustomerResponse response = customerService.updateCustomerStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Customer status updated successfully", response));
    }

    private String resolveAuthenticatedEmail(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            String preferred = jwt.getClaimAsString("preferred_username");
            if (preferred != null && preferred.contains("@")) {
                return preferred;
            }
            String email = jwt.getClaimAsString("email");
            if (email != null) {
                return email;
            }
        }
        throw new IllegalStateException("Authenticated principal does not contain an email claim");
    }
}
