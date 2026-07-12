package com.cloudbank.digitalbanking.account.dto;

import com.cloudbank.digitalbanking.account.enums.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request payload for updating account status")
public class AccountStatusUpdateRequest {

    @NotNull(message = "Account status is required")
    private AccountStatus accountStatus;
}
