package com.cloudbank.digitalbanking.transaction.dto;

import com.cloudbank.digitalbanking.transaction.enums.TransactionStatus;
import com.cloudbank.digitalbanking.transaction.enums.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "Transaction response payload")
public class TransactionResponse {

    private UUID id;
    private String transactionReference;
    private UUID accountId;
    private UUID relatedAccountId;
    private UUID paymentId;
    private TransactionType transactionType;
    private BigDecimal amount;
    private String currency;
    private String description;
    private TransactionStatus status;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private LocalDateTime createdAt;
}
