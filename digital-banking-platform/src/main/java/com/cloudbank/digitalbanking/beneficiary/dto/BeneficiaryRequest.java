package com.cloudbank.digitalbanking.beneficiary.dto;

import com.cloudbank.digitalbanking.common.constants.BankingValidationPatterns;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Schema(description = "Request payload for creating a beneficiary")
public class BeneficiaryRequest {

    @NotNull(message = "Customer ID is required")
    private UUID customerId;

    @NotBlank(message = "Beneficiary name is required")
    @Size(max = 150, message = "Beneficiary name must not exceed 150 characters")
    private String beneficiaryName;

    @NotBlank(message = "Beneficiary account number is required")
    @Pattern(
            regexp = BankingValidationPatterns.BENEFICIARY_ACCOUNT_NUMBER,
            message = "Beneficiary account number must be 7 to 12 digits or an internal CloudBank number (CB + 10 digits)"
    )
    private String beneficiaryAccountNumber;

    @NotBlank(message = "Bank name is required")
    @Size(max = 150, message = "Bank name must not exceed 150 characters")
    private String bankName;

    @NotBlank(message = "Transit number is required")
    @Pattern(
            regexp = "^\\d{5}$",
            message = "Transit number must be a 5-digit Canadian transit number"
    )
    private String transitNumber;

    @NotBlank(message = "Institution number is required")
    @Pattern(
            regexp = "^\\d{3}$",
            message = "Institution number must be a 3-digit Canadian institution number"
    )
    private String institutionNumber;

    @Size(max = 50, message = "Nickname must not exceed 50 characters")
    private String nickname;
}
