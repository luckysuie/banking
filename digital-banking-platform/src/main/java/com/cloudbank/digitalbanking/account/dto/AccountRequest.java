package com.cloudbank.digitalbanking.account.dto;

import com.cloudbank.digitalbanking.account.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Request payload for creating a new account")
public class AccountRequest {

    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @DecimalMin(value = "0.01", message = "Daily transfer limit must be greater than zero")
    @Digits(integer = 17, fraction = 2, message = "Daily transfer limit must have at most 17 integer digits and 2 decimal places")
    @Schema(description = "Optional daily transfer limit; defaults to 10000.00 CAD")
    private BigDecimal dailyTransferLimit;
}
