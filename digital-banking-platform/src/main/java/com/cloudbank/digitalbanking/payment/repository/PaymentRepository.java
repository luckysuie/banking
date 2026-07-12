package com.cloudbank.digitalbanking.payment.repository;

import com.cloudbank.digitalbanking.payment.entity.Payment;
import com.cloudbank.digitalbanking.payment.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    Optional<Payment> findByPaymentReference(String paymentReference);

    List<Payment> findBySourceAccountIdOrderByCreatedAtDesc(UUID sourceAccountId);

    @Query("""
            SELECT p FROM Payment p
            WHERE p.sourceAccountId IN (
                SELECT a.id FROM com.cloudbank.digitalbanking.account.entity.Account a
                WHERE a.customerId = :customerId
            )
            ORDER BY p.createdAt DESC
            """)
    List<Payment> findByCustomerId(@Param("customerId") UUID customerId);

    @Query("""
            SELECT COALESCE(SUM(p.amount), 0)
            FROM Payment p
            WHERE p.sourceAccountId = :sourceAccountId
              AND p.status IN :statuses
              AND p.createdAt >= :startOfDay
            """)
    BigDecimal sumTransferAmountBySourceAccountSince(
            @Param("sourceAccountId") UUID sourceAccountId,
            @Param("statuses") Collection<PaymentStatus> statuses,
            @Param("startOfDay") Instant startOfDay);
}
