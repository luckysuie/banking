package com.cloudbank.digitalbanking.transaction.service;

import com.cloudbank.digitalbanking.transaction.entity.Transaction;
import com.cloudbank.digitalbanking.transaction.exception.TransactionNotFoundException;
import com.cloudbank.digitalbanking.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private com.cloudbank.digitalbanking.account.repository.AccountRepository accountRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    void getTransactionById_shouldThrowTransactionNotFoundException_whenTransactionDoesNotExist() {
        UUID transactionId = UUID.randomUUID();
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransactionById(transactionId))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessageContaining(transactionId.toString());
    }

    @Test
    void getTransactionByReference_shouldThrowTransactionNotFoundException_whenReferenceDoesNotExist() {
        String reference = "TXN-A1B2C3D4E5F6";
        when(transactionRepository.findByTransactionReference(reference)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransactionByReference(reference))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessageContaining(reference);
    }
}
