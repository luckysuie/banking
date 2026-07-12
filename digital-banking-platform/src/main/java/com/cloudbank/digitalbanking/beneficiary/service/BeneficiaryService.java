package com.cloudbank.digitalbanking.beneficiary.service;

import com.cloudbank.digitalbanking.beneficiary.dto.BeneficiaryRequest;
import com.cloudbank.digitalbanking.beneficiary.dto.BeneficiaryResponse;
import com.cloudbank.digitalbanking.beneficiary.dto.BeneficiaryUpdateRequest;
import com.cloudbank.digitalbanking.beneficiary.enums.BeneficiaryStatus;

import java.util.List;
import java.util.UUID;

public interface BeneficiaryService {

    BeneficiaryResponse createBeneficiary(BeneficiaryRequest request);

    BeneficiaryResponse getBeneficiaryById(UUID id);

    List<BeneficiaryResponse> getBeneficiariesByCustomerId(UUID customerId);

    BeneficiaryResponse updateBeneficiary(UUID id, BeneficiaryUpdateRequest request);

    BeneficiaryResponse updateBeneficiaryStatus(UUID id, BeneficiaryStatus status);

    void deleteBeneficiary(UUID id);
}
