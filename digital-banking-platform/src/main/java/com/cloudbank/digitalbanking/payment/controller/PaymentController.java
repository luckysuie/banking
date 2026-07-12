package com.cloudbank.digitalbanking.payment.controller;

import com.cloudbank.digitalbanking.common.dto.ApiResponse;
import com.cloudbank.digitalbanking.payment.dto.FundTransferRequest;
import com.cloudbank.digitalbanking.payment.dto.PaymentResponse;
import com.cloudbank.digitalbanking.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payment & Fund Transfer", description = "APIs for processing fund transfers and viewing payment history")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/transfers")
    @Operation(
            summary = "Process a fund transfer",
            description = """
                    Transfers funds from a source account to a beneficiary account. \
                    Validates account status, beneficiary ownership, balance, and daily limits. \
                    Supports idempotent processing via idempotencyKey."""
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201", description = "Transfer completed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Invalid transfer or insufficient balance"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Source account or beneficiary not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409", description = "Duplicate idempotency key with conflicting details")
    })
    public ResponseEntity<ApiResponse<PaymentResponse>> processFundTransfer(
            @Valid @RequestBody FundTransferRequest request) {
        PaymentResponse response = paymentService.processFundTransfer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Fund transfer completed successfully", response));
    }

    @GetMapping("/reference/{paymentReference}")
    @Operation(summary = "Get payment by reference")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Payment found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByReference(
            @Parameter(description = "Unique payment reference (e.g. PAY-A1B2C3D4E5F6)")
            @PathVariable String paymentReference) {
        return ResponseEntity.ok(ApiResponse.success(
                paymentService.getPaymentByReference(paymentReference)));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get all payments for a customer")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Payments retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByCustomer(
            @Parameter(description = "Customer UUID") @PathVariable UUID customerId) {
        return ResponseEntity.ok(ApiResponse.success(
                paymentService.getPaymentsByCustomerId(customerId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Payment found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(
            @Parameter(description = "Payment UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentById(id)));
    }
}
