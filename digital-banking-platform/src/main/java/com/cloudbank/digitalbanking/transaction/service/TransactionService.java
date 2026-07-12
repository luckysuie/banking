package com.cloudbank.digitalbanking.transaction.service;

import com.cloudbank.digitalbanking.common.dto.PageResponse;
import com.cloudbank.digitalbanking.transaction.dto.TransactionResponse;
import com.cloudbank.digitalbanking.transaction.enums.TransactionStatus;
import com.cloudbank.digitalbanking.transaction.enums.TransactionType;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TransactionService {

    TransactionResponse getTransactionById(UUID id);

    TransactionResponse getTransactionByReference(String transactionReference);

    PageResponse<TransactionResponse> getTransactionsByAccountId(UUID accountId, Pageable pageable);

    PageResponse<TransactionResponse> getFilteredTransactionsByAccountId(
            UUID accountId,
            TransactionType transactionType,
            TransactionStatus status,
            Pageable pageable);
}
