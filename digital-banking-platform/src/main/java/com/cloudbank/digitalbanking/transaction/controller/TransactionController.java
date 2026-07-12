package com.cloudbank.digitalbanking.transaction.controller;

import com.cloudbank.digitalbanking.common.dto.ApiResponse;
import com.cloudbank.digitalbanking.common.dto.PageResponse;
import com.cloudbank.digitalbanking.transaction.dto.TransactionResponse;
import com.cloudbank.digitalbanking.transaction.enums.TransactionStatus;
import com.cloudbank.digitalbanking.transaction.enums.TransactionType;
import com.cloudbank.digitalbanking.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Management", description = "Read-only APIs for transaction history")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/reference/{transactionReference}")
    @Operation(summary = "Get transaction by reference")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Transaction found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionByReference(
            @Parameter(description = "Unique transaction reference (e.g. TXN-A1B2C3D4E5F6)")
            @PathVariable String transactionReference) {
        return ResponseEntity.ok(ApiResponse.success(
                transactionService.getTransactionByReference(transactionReference)));
    }

    @GetMapping("/account/{accountId}/filter")
    @Operation(
            summary = "Get filtered transactions for an account",
            description = "Supports optional filtering by transaction type and status with pagination"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Transactions retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getFilteredTransactionsByAccount(
            @Parameter(description = "Account UUID") @PathVariable UUID accountId,
            @Parameter(description = "Optional transaction type filter")
            @RequestParam(required = false) TransactionType transactionType,
            @Parameter(description = "Optional transaction status filter")
            @RequestParam(required = false) TransactionStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                transactionService.getFilteredTransactionsByAccountId(
                        accountId, transactionType, status, pageable)));
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get paginated transaction history for an account")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Transactions retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Account not found")
    })
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getTransactionsByAccount(
            @Parameter(description = "Account UUID") @PathVariable UUID accountId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                transactionService.getTransactionsByAccountId(accountId, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Transaction found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Transaction not found")
    })
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransactionById(
            @Parameter(description = "Transaction UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(transactionService.getTransactionById(id)));
    }
}
