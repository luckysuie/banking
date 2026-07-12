package com.cloudbank.digitalbanking.transaction.repository;

import com.cloudbank.digitalbanking.transaction.entity.Transaction;
import com.cloudbank.digitalbanking.transaction.enums.TransactionStatus;
import com.cloudbank.digitalbanking.transaction.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByTransactionReference(String transactionReference);

    Page<Transaction> findByAccountIdOrderByCreatedAtDesc(UUID accountId, Pageable pageable);

    @Query("""
            SELECT t FROM Transaction t
            WHERE t.accountId = :accountId
              AND (:transactionType IS NULL OR t.transactionType = :transactionType)
              AND (:status IS NULL OR t.status = :status)
            ORDER BY t.createdAt DESC
            """)
    Page<Transaction> findByAccountIdWithFilters(
            @Param("accountId") UUID accountId,
            @Param("transactionType") TransactionType transactionType,
            @Param("status") TransactionStatus status,
            Pageable pageable);
}
