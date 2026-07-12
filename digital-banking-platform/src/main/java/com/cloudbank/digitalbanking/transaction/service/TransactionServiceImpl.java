package com.cloudbank.digitalbanking.transaction.service;

import com.cloudbank.digitalbanking.account.exception.AccountNotFoundException;
import com.cloudbank.digitalbanking.account.repository.AccountRepository;
import com.cloudbank.digitalbanking.common.dto.PageResponse;
import com.cloudbank.digitalbanking.transaction.dto.TransactionResponse;
import com.cloudbank.digitalbanking.transaction.entity.Transaction;
import com.cloudbank.digitalbanking.transaction.enums.TransactionStatus;
import com.cloudbank.digitalbanking.transaction.enums.TransactionType;
import com.cloudbank.digitalbanking.transaction.exception.TransactionNotFoundException;
import com.cloudbank.digitalbanking.transaction.mapper.TransactionMapper;
import com.cloudbank.digitalbanking.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Override
    public TransactionResponse getTransactionById(UUID id) {
        return TransactionMapper.toResponse(findTransactionOrThrow(id));
    }

    @Override
    public TransactionResponse getTransactionByReference(String transactionReference) {
        Transaction transaction = transactionRepository.findByTransactionReference(transactionReference)
                .orElseThrow(() -> new TransactionNotFoundException(
                        "Transaction not found with reference: " + transactionReference));
        return TransactionMapper.toResponse(transaction);
    }

    @Override
    public PageResponse<TransactionResponse> getTransactionsByAccountId(UUID accountId, Pageable pageable) {
        validateAccountExists(accountId);
        Page<Transaction> page = transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId, pageable);
        return PageResponse.from(page, TransactionMapper::toResponse);
    }

    @Override
    public PageResponse<TransactionResponse> getFilteredTransactionsByAccountId(
            UUID accountId,
            TransactionType transactionType,
            TransactionStatus status,
            Pageable pageable) {
        validateAccountExists(accountId);
        Page<Transaction> page = transactionRepository.findByAccountIdWithFilters(
                accountId, transactionType, status, pageable);
        return PageResponse.from(page, TransactionMapper::toResponse);
    }

    private Transaction findTransactionOrThrow(UUID id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found with id: " + id));
    }

    private void validateAccountExists(UUID accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new AccountNotFoundException("Account not found with id: " + accountId);
        }
    }
}
