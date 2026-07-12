package com.cloudbank.digitalbanking.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "Account balance summary")
public class AccountBalanceResponse {

    private UUID accountId;
    private String accountNumber;
    private String currency;
    private BigDecimal currentBalance;
    private BigDecimal availableBalance;
}
