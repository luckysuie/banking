package com.cloudbank.digitalbanking.beneficiary.repository;

import com.cloudbank.digitalbanking.beneficiary.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, UUID> {

    List<Beneficiary> findByCustomerId(UUID customerId);

    boolean existsByCustomerIdAndBeneficiaryAccountNumber(UUID customerId, String beneficiaryAccountNumber);

    boolean existsByCustomerIdAndBeneficiaryAccountNumberAndIdNot(
            UUID customerId,
            String beneficiaryAccountNumber,
            UUID id);
}
