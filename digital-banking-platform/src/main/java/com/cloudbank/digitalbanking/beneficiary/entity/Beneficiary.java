package com.cloudbank.digitalbanking.beneficiary.entity;

import com.cloudbank.digitalbanking.beneficiary.enums.BeneficiaryStatus;
import com.cloudbank.digitalbanking.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "beneficiaries",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"customer_id", "beneficiary_account_number"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class Beneficiary extends BaseEntity {

    @Column(nullable = false)
    private UUID customerId;

    @Column(nullable = false, length = 150)
    private String beneficiaryName;

    @Column(nullable = false, length = 20)
    private String beneficiaryAccountNumber;

    @Column(nullable = false, length = 150)
    private String bankName;

    @Column(nullable = false, length = 5)
    private String transitNumber;

    @Column(nullable = false, length = 3)
    private String institutionNumber;

    @Column(length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BeneficiaryStatus status = BeneficiaryStatus.ACTIVE;
}
