package com.cloudbank.digitalbanking.account.dto;

import com.cloudbank.digitalbanking.account.enums.AccountStatus;
import com.cloudbank.digitalbanking.account.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "Account response payload")
public class AccountResponse {

    private UUID id;
    private String accountNumber;
    private UUID customerId;
    private AccountType accountType;
    private AccountStatus accountStatus;
    private String currency;
    private BigDecimal currentBalance;
    private BigDecimal availableBalance;
    private BigDecimal dailyTransferLimit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
