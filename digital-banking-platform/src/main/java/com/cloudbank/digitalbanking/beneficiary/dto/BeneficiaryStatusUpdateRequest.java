package com.cloudbank.digitalbanking.beneficiary.dto;

import com.cloudbank.digitalbanking.beneficiary.enums.BeneficiaryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request payload for updating beneficiary status")
public class BeneficiaryStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private BeneficiaryStatus status;
}
