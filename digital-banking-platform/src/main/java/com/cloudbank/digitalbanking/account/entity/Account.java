package com.cloudbank.digitalbanking.account.entity;

import com.cloudbank.digitalbanking.account.enums.AccountStatus;
import com.cloudbank.digitalbanking.account.enums.AccountType;
import com.cloudbank.digitalbanking.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "accounts",
        indexes = {
                @Index(name = "idx_accounts_customer_id", columnList = "customer_id"),
                @Index(name = "idx_accounts_account_number", columnList = "account_number")
        }
)
@Check(constraints = "current_balance >= 0 AND available_balance >= 0")
@Getter
@Setter
@NoArgsConstructor
public class Account extends BaseEntity {

    @Column(nullable = false, unique = true, updatable = false, length = 20)
    private String accountNumber;

    @Column(nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal availableBalance = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal dailyTransferLimit;

    @PrePersist
    @PreUpdate
    private void validateBalances() {
        if (currentBalance != null && currentBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Current balance cannot be negative");
        }
        if (availableBalance != null && availableBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Available balance cannot be negative");
        }
    }
}
