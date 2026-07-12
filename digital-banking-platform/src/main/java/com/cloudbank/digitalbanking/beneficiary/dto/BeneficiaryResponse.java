package com.cloudbank.digitalbanking.beneficiary.dto;

import com.cloudbank.digitalbanking.beneficiary.enums.BeneficiaryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "Beneficiary response payload")
public class BeneficiaryResponse {

    private UUID id;
    private UUID customerId;
    private String beneficiaryName;
    private String beneficiaryAccountNumber;
    private String bankName;
    private String transitNumber;
    private String institutionNumber;
    private String nickname;
    private BeneficiaryStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
