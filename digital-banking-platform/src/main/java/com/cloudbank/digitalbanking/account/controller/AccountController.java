package com.cloudbank.digitalbanking.account.controller;

import com.cloudbank.digitalbanking.account.dto.AccountBalanceResponse;
import com.cloudbank.digitalbanking.account.dto.AccountRequest;
import com.cloudbank.digitalbanking.account.dto.AccountResponse;
import com.cloudbank.digitalbanking.account.dto.AccountStatusUpdateRequest;
import com.cloudbank.digitalbanking.account.service.AccountService;
import com.cloudbank.digitalbanking.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Account Management", description = "APIs for managing customer bank accounts")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Create a new account", description = "Opens a new account for an existing customer")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "Account created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Valid @RequestBody AccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created successfully", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account by ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Account found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountById(
            @Parameter(description = "Account UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(accountService.getAccountById(id)));
    }

    @GetMapping("/account-number/{accountNumber}")
    @Operation(summary = "Get account by account number")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Account found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountByAccountNumber(
            @Parameter(description = "Unique account number (e.g. CB4829103756)")
            @PathVariable String accountNumber) {
        return ResponseEntity.ok(ApiResponse.success(
                accountService.getAccountByAccountNumber(accountNumber)));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get all accounts for a customer")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Accounts retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAccountsByCustomer(
            @Parameter(description = "Customer UUID") @PathVariable UUID customerId) {
        return ResponseEntity.ok(ApiResponse.success(
                accountService.getAccountsByCustomerId(customerId)));
    }

    @GetMapping("/{id}/balance")
    @Operation(summary = "Get account balance")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Balance retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<ApiResponse<AccountBalanceResponse>> getAccountBalance(
            @Parameter(description = "Account UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(accountService.getAccountBalance(id)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update account status")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Account status updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccountStatus(
            @Parameter(description = "Account UUID") @PathVariable UUID id,
            @Valid @RequestBody AccountStatusUpdateRequest request) {
        AccountResponse response = accountService.updateAccountStatus(id, request.getAccountStatus());
        return ResponseEntity.ok(ApiResponse.success("Account status updated successfully", response));
    }
}
