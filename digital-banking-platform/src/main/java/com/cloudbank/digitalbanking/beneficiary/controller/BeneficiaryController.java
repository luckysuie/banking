package com.cloudbank.digitalbanking.beneficiary.controller;

import com.cloudbank.digitalbanking.beneficiary.dto.BeneficiaryRequest;
import com.cloudbank.digitalbanking.beneficiary.dto.BeneficiaryResponse;
import com.cloudbank.digitalbanking.beneficiary.dto.BeneficiaryStatusUpdateRequest;
import com.cloudbank.digitalbanking.beneficiary.dto.BeneficiaryUpdateRequest;
import com.cloudbank.digitalbanking.beneficiary.service.BeneficiaryService;
import com.cloudbank.digitalbanking.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/beneficiaries")
@RequiredArgsConstructor
@Tag(name = "Beneficiary Management", description = "APIs for managing customer payment beneficiaries")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    @PostMapping
    @Operation(summary = "Create a new beneficiary")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "Beneficiary created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Validation failed or duplicate account number"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<ApiResponse<BeneficiaryResponse>> createBeneficiary(
            @Valid @RequestBody BeneficiaryRequest request) {
        BeneficiaryResponse response = beneficiaryService.createBeneficiary(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Beneficiary created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get beneficiary by ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Beneficiary found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Beneficiary not found")
    })
    public ResponseEntity<ApiResponse<BeneficiaryResponse>> getBeneficiaryById(
            @Parameter(description = "Beneficiary UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(beneficiaryService.getBeneficiaryById(id)));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get all beneficiaries for a customer")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Beneficiaries retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<ApiResponse<List<BeneficiaryResponse>>> getBeneficiariesByCustomer(
            @Parameter(description = "Customer UUID") @PathVariable UUID customerId) {
        return ResponseEntity.ok(ApiResponse.success(
                beneficiaryService.getBeneficiariesByCustomerId(customerId)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update beneficiary details")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Beneficiary updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Validation failed or duplicate account number"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Beneficiary not found")
    })
    public ResponseEntity<ApiResponse<BeneficiaryResponse>> updateBeneficiary(
            @Parameter(description = "Beneficiary UUID") @PathVariable UUID id,
            @Valid @RequestBody BeneficiaryUpdateRequest request) {
        BeneficiaryResponse response = beneficiaryService.updateBeneficiary(id, request);
        return ResponseEntity.ok(ApiResponse.success("Beneficiary updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update beneficiary status")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Beneficiary status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Beneficiary not found")
    })
    public ResponseEntity<ApiResponse<BeneficiaryResponse>> updateBeneficiaryStatus(
            @Parameter(description = "Beneficiary UUID") @PathVariable UUID id,
            @Valid @RequestBody BeneficiaryStatusUpdateRequest request) {
        BeneficiaryResponse response = beneficiaryService.updateBeneficiaryStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Beneficiary status updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a beneficiary")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Beneficiary deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Beneficiary not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteBeneficiary(
            @Parameter(description = "Beneficiary UUID") @PathVariable UUID id) {
        beneficiaryService.deleteBeneficiary(id);
        return ResponseEntity.ok(ApiResponse.success("Beneficiary deleted successfully", null));
    }
}
