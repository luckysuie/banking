package com.cloudbank.digitalbanking.payment.dto;

import com.cloudbank.digitalbanking.payment.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "Payment response payload")
public class PaymentResponse {

    private UUID id;
    private String paymentReference;
    private UUID sourceAccountId;
    private UUID beneficiaryId;
    private String destinationAccountNumber;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String idempotencyKey;
    private PaymentStatus status;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
