package com.cloudbank.digitalbanking.transaction.mapper;

import com.cloudbank.digitalbanking.common.constants.BankingConstants;
import com.cloudbank.digitalbanking.common.util.ReferenceGenerator;
import com.cloudbank.digitalbanking.transaction.dto.TransactionResponse;
import com.cloudbank.digitalbanking.transaction.entity.Transaction;
import com.cloudbank.digitalbanking.transaction.enums.TransactionStatus;
import com.cloudbank.digitalbanking.transaction.enums.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

public final class TransactionMapper {

    private TransactionMapper() {
    }

    public static TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .transactionReference(transaction.getTransactionReference())
                .accountId(transaction.getAccountId())
                .relatedAccountId(transaction.getRelatedAccountId())
                .paymentId(transaction.getPaymentId())
                .transactionType(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .description(transaction.getDescription())
                .status(transaction.getStatus())
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

    /**
     * Builds a new transaction record for internal use by payment and transfer services.
     */
    public static Transaction createRecord(
            UUID accountId,
            UUID relatedAccountId,
            UUID paymentId,
            TransactionType transactionType,
            BigDecimal amount,
            String currency,
            String description,
            TransactionStatus status,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter) {
        Transaction transaction = new Transaction();
        transaction.setTransactionReference(ReferenceGenerator.generateTransactionReference());
        transaction.setAccountId(accountId);
        transaction.setRelatedAccountId(relatedAccountId);
        transaction.setPaymentId(paymentId);
        transaction.setTransactionType(transactionType);
        transaction.setAmount(com.cloudbank.digitalbanking.common.util.MoneyUtils.normalize(amount));
        transaction.setCurrency(currency != null ? currency : BankingConstants.DEFAULT_ACCOUNT_CURRENCY);
        transaction.setDescription(description);
        transaction.setStatus(status);
        transaction.setBalanceBefore(com.cloudbank.digitalbanking.common.util.MoneyUtils.normalize(balanceBefore));
        transaction.setBalanceAfter(com.cloudbank.digitalbanking.common.util.MoneyUtils.normalize(balanceAfter));
        return transaction;
    }
}
