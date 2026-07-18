package com.cloudbank.digitalbanking.payment.entity;

import com.cloudbank.digitalbanking.common.entity.BaseEntity;
import com.cloudbank.digitalbanking.payment.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "payments",
        indexes = {
                @Index(name = "idx_payments_idempotency_key", columnList = "idempotency_key"),
                @Index(name = "idx_payments_source_account_id", columnList = "source_account_id"),
                @Index(name = "idx_payments_created_at", columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Payment extends BaseEntity {

    @Column(nullable = false, unique = true, updatable = false, length = 20)
    private String paymentReference;

    @Column(nullable = false)
    private UUID sourceAccountId;

    @Column(nullable = false)
    private UUID beneficiaryId;

    @Column(nullable = false, length = 20)
    private String destinationAccountNumber;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.RECEIVED;

    @Column(length = 500)
    private String failureReason;

    private LocalDateTime completedAt;
}
