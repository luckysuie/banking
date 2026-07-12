package com.cloudbank.digitalbanking.beneficiary.mapper;

import com.cloudbank.digitalbanking.beneficiary.dto.BeneficiaryRequest;
import com.cloudbank.digitalbanking.beneficiary.dto.BeneficiaryResponse;
import com.cloudbank.digitalbanking.beneficiary.dto.BeneficiaryUpdateRequest;
import com.cloudbank.digitalbanking.beneficiary.entity.Beneficiary;
import com.cloudbank.digitalbanking.beneficiary.enums.BeneficiaryStatus;

public final class BeneficiaryMapper {

    private BeneficiaryMapper() {
    }

    public static Beneficiary toEntity(BeneficiaryRequest request) {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setCustomerId(request.getCustomerId());
        applyDetails(beneficiary, request.getBeneficiaryName(), request.getBeneficiaryAccountNumber(),
                request.getBankName(), request.getTransitNumber(), request.getInstitutionNumber(),
                request.getNickname());
        beneficiary.setStatus(BeneficiaryStatus.ACTIVE);
        return beneficiary;
    }

    public static void updateEntity(Beneficiary beneficiary, BeneficiaryUpdateRequest request) {
        applyDetails(beneficiary, request.getBeneficiaryName(), request.getBeneficiaryAccountNumber(),
                request.getBankName(), request.getTransitNumber(), request.getInstitutionNumber(),
                request.getNickname());
    }

    public static BeneficiaryResponse toResponse(Beneficiary beneficiary) {
        return BeneficiaryResponse.builder()
                .id(beneficiary.getId())
                .customerId(beneficiary.getCustomerId())
                .beneficiaryName(beneficiary.getBeneficiaryName())
                .beneficiaryAccountNumber(beneficiary.getBeneficiaryAccountNumber())
                .bankName(beneficiary.getBankName())
                .transitNumber(beneficiary.getTransitNumber())
                .institutionNumber(beneficiary.getInstitutionNumber())
                .nickname(beneficiary.getNickname())
                .status(beneficiary.getStatus())
                .createdAt(beneficiary.getCreatedAt())
                .updatedAt(beneficiary.getUpdatedAt())
                .build();
    }

    private static void applyDetails(
            Beneficiary beneficiary,
            String beneficiaryName,
            String beneficiaryAccountNumber,
            String bankName,
            String transitNumber,
            String institutionNumber,
            String nickname) {
        beneficiary.setBeneficiaryName(beneficiaryName.trim());
        beneficiary.setBeneficiaryAccountNumber(beneficiaryAccountNumber.trim());
        beneficiary.setBankName(bankName.trim());
        beneficiary.setTransitNumber(transitNumber.trim());
        beneficiary.setInstitutionNumber(institutionNumber.trim());
        beneficiary.setNickname(nickname != null ? nickname.trim() : null);
    }
}
