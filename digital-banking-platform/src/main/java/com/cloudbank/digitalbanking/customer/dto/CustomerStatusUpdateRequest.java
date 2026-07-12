package com.cloudbank.digitalbanking.customer.dto;

import com.cloudbank.digitalbanking.customer.enums.CustomerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request payload for updating customer status")
public class CustomerStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private CustomerStatus status;
}
