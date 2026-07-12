package com.cloudbank.digitalbanking.payment.service;

import com.cloudbank.digitalbanking.account.entity.Account;
import com.cloudbank.digitalbanking.account.enums.AccountStatus;
import com.cloudbank.digitalbanking.account.repository.AccountRepository;
import com.cloudbank.digitalbanking.audit.enums.AuditAction;
import com.cloudbank.digitalbanking.audit.service.AuditService;
import com.cloudbank.digitalbanking.beneficiary.entity.Beneficiary;
import com.cloudbank.digitalbanking.beneficiary.enums.BeneficiaryStatus;
import com.cloudbank.digitalbanking.beneficiary.repository.BeneficiaryRepository;
import com.cloudbank.digitalbanking.customer.repository.CustomerRepository;
import com.cloudbank.digitalbanking.notification.service.NotificationService;
import com.cloudbank.digitalbanking.payment.dto.FundTransferRequest;
import com.cloudbank.digitalbanking.payment.dto.PaymentResponse;
import com.cloudbank.digitalbanking.payment.entity.Payment;
import com.cloudbank.digitalbanking.payment.enums.PaymentStatus;
import com.cloudbank.digitalbanking.payment.exception.DuplicatePaymentException;
import com.cloudbank.digitalbanking.payment.exception.InsufficientBalanceException;
import com.cloudbank.digitalbanking.payment.exception.InvalidTransferException;
import com.cloudbank.digitalbanking.payment.repository.PaymentRepository;
import com.cloudbank.digitalbanking.transaction.entity.Transaction;
import com.cloudbank.digitalbanking.transaction.enums.TransactionStatus;
import com.cloudbank.digitalbanking.transaction.enums.TransactionType;
import com.cloudbank.digitalbanking.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private BeneficiaryRepository beneficiaryRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void processFundTransfer_shouldCompleteTransferAndCreateDebitTransaction_whenRequestIsValid() {
        FundTransferRequest request = buildTransferRequest();
        Account sourceAccount = buildSourceAccount(request.getSourceAccountId());
        Beneficiary beneficiary = buildBeneficiary(request.getBeneficiaryId(), sourceAccount.getCustomerId());

        when(paymentRepository.findByIdempotencyKey(request.getIdempotencyKey())).thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate(request.getSourceAccountId())).thenReturn(Optional.of(sourceAccount));
        when(beneficiaryRepository.findById(request.getBeneficiaryId())).thenReturn(Optional.of(beneficiary));
        when(paymentRepository.sumTransferAmountBySourceAccountSince(any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            if (payment.getId() == null) {
                payment.setId(UUID.randomUUID());
            }
            return payment;
        });
        when(accountRepository.findByAccountNumber(beneficiary.getBeneficiaryAccountNumber()))
                .thenReturn(Optional.empty());
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.processFundTransfer(request);

        assertThat(response.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(response.getAmount()).isEqualByComparingTo("100.00");

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository, times(1)).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue().getAvailableBalance()).isEqualByComparingTo("900.00");

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(transactionCaptor.capture());
        Transaction debitTransaction = transactionCaptor.getValue();
        assertThat(debitTransaction.getTransactionType()).isEqualTo(TransactionType.DEBIT);
        assertThat(debitTransaction.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(debitTransaction.getBalanceBefore()).isEqualByComparingTo("1000.00");
        assertThat(debitTransaction.getBalanceAfter()).isEqualByComparingTo("900.00");

        verify(notificationService).notifyPaymentSuccess(
                eq(sourceAccount.getCustomerId()),
                any(String.class),
                eq(request.getAmount()),
                eq("CAD"));
        verify(auditService).recordEvent(
                eq(sourceAccount.getCustomerId().toString()),
                eq(AuditAction.PAYMENT_COMPLETED),
                any(String.class),
                any(String.class),
                any(String.class),
                any());
    }

    @Test
    void processFundTransfer_shouldCreditInternalDestination_whenBeneficiaryUsesCloudBankAccountNumber() {
        FundTransferRequest request = buildTransferRequest();
        Account sourceAccount = buildSourceAccount(request.getSourceAccountId());
        Account destinationAccount = buildSourceAccount(UUID.randomUUID());
        destinationAccount.setAccountNumber("CB9876543210");
        destinationAccount.setCustomerId(sourceAccount.getCustomerId());

        Beneficiary beneficiary = buildBeneficiary(request.getBeneficiaryId(), sourceAccount.getCustomerId());
        beneficiary.setBeneficiaryAccountNumber(destinationAccount.getAccountNumber());

        when(paymentRepository.findByIdempotencyKey(request.getIdempotencyKey())).thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate(request.getSourceAccountId())).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByIdForUpdate(destinationAccount.getId())).thenReturn(Optional.of(destinationAccount));
        when(beneficiaryRepository.findById(request.getBeneficiaryId())).thenReturn(Optional.of(beneficiary));
        when(paymentRepository.sumTransferAmountBySourceAccountSince(any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(UUID.randomUUID());
            return payment;
        });
        when(accountRepository.findByAccountNumber(destinationAccount.getAccountNumber()))
                .thenReturn(Optional.of(destinationAccount));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.processFundTransfer(request);

        assertThat(response.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        verify(transactionRepository, times(2)).save(any(Transaction.class));
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    void processFundTransfer_shouldThrowInsufficientBalanceException_whenBalanceIsTooLow() {
        FundTransferRequest request = buildTransferRequest();
        Account sourceAccount = buildSourceAccount(request.getSourceAccountId());
        sourceAccount.setAvailableBalance(new BigDecimal("10.00"));
        sourceAccount.setCurrentBalance(new BigDecimal("10.00"));
        Beneficiary beneficiary = buildBeneficiary(request.getBeneficiaryId(), sourceAccount.getCustomerId());

        when(paymentRepository.findByIdempotencyKey(request.getIdempotencyKey())).thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate(request.getSourceAccountId())).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(request.getSourceAccountId())).thenReturn(Optional.of(sourceAccount));
        when(beneficiaryRepository.findById(request.getBeneficiaryId())).thenReturn(Optional.of(beneficiary));
        when(paymentRepository.sumTransferAmountBySourceAccountSince(any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        assertThatThrownBy(() -> paymentService.processFundTransfer(request))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("Insufficient available balance");

        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(notificationService).notifyPaymentFailed(eq(sourceAccount.getCustomerId()), any(String.class));
    }

    @Test
    void processFundTransfer_shouldThrowInvalidTransferException_whenTransferExceedsDailyLimit() {
        FundTransferRequest request = buildTransferRequest();
        Account sourceAccount = buildSourceAccount(request.getSourceAccountId());
        sourceAccount.setDailyTransferLimit(new BigDecimal("100.00"));
        Beneficiary beneficiary = buildBeneficiary(request.getBeneficiaryId(), sourceAccount.getCustomerId());

        when(paymentRepository.findByIdempotencyKey(request.getIdempotencyKey())).thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate(request.getSourceAccountId())).thenReturn(Optional.of(sourceAccount));
        when(beneficiaryRepository.findById(request.getBeneficiaryId())).thenReturn(Optional.of(beneficiary));
        when(paymentRepository.sumTransferAmountBySourceAccountSince(any(), any(), any()))
                .thenReturn(new BigDecimal("50.00"));

        assertThatThrownBy(() -> paymentService.processFundTransfer(request))
                .isInstanceOf(InvalidTransferException.class)
                .hasMessageContaining("daily transfer limit");

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void processFundTransfer_shouldReturnExistingPayment_whenIdempotencyKeyMatchesSameRequest() {
        FundTransferRequest request = buildTransferRequest();
        Payment existingPayment = new Payment();
        existingPayment.setId(UUID.randomUUID());
        existingPayment.setPaymentReference("PAY-EXISTING123");
        existingPayment.setSourceAccountId(request.getSourceAccountId());
        existingPayment.setBeneficiaryId(request.getBeneficiaryId());
        existingPayment.setDestinationAccountNumber("9876543210");
        existingPayment.setAmount(request.getAmount());
        existingPayment.setCurrency("CAD");
        existingPayment.setIdempotencyKey(request.getIdempotencyKey());
        existingPayment.setStatus(PaymentStatus.COMPLETED);

        when(paymentRepository.findByIdempotencyKey(request.getIdempotencyKey()))
                .thenReturn(Optional.of(existingPayment));

        PaymentResponse response = paymentService.processFundTransfer(request);

        assertThat(response.getId()).isEqualTo(existingPayment.getId());
        assertThat(response.getPaymentReference()).isEqualTo("PAY-EXISTING123");
        verify(accountRepository, never()).findByIdForUpdate(any());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void processFundTransfer_shouldThrowDuplicatePaymentException_whenIdempotencyKeyReusedWithDifferentDetails() {
        FundTransferRequest request = buildTransferRequest();
        Payment existingPayment = new Payment();
        existingPayment.setSourceAccountId(request.getSourceAccountId());
        existingPayment.setBeneficiaryId(request.getBeneficiaryId());
        existingPayment.setAmount(new BigDecimal("50.00"));
        existingPayment.setIdempotencyKey(request.getIdempotencyKey());

        when(paymentRepository.findByIdempotencyKey(request.getIdempotencyKey()))
                .thenReturn(Optional.of(existingPayment));

        assertThatThrownBy(() -> paymentService.processFundTransfer(request))
                .isInstanceOf(DuplicatePaymentException.class)
                .hasMessageContaining("different payment details");
    }

    @Test
    void processFundTransfer_shouldThrowInvalidTransferException_whenSourceAndDestinationAccountsAreTheSame() {
        FundTransferRequest request = buildTransferRequest();
        Account sourceAccount = buildSourceAccount(request.getSourceAccountId());
        Beneficiary beneficiary = buildBeneficiary(request.getBeneficiaryId(), sourceAccount.getCustomerId());
        beneficiary.setBeneficiaryAccountNumber(sourceAccount.getAccountNumber());

        when(paymentRepository.findByIdempotencyKey(request.getIdempotencyKey())).thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate(request.getSourceAccountId())).thenReturn(Optional.of(sourceAccount));
        when(beneficiaryRepository.findById(request.getBeneficiaryId())).thenReturn(Optional.of(beneficiary));

        assertThatThrownBy(() -> paymentService.processFundTransfer(request))
                .isInstanceOf(InvalidTransferException.class)
                .hasMessageContaining("cannot be the same");

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void processFundTransfer_shouldThrowInvalidTransferException_whenSourceAccountIsNotActive() {
        FundTransferRequest request = buildTransferRequest();
        Account sourceAccount = buildSourceAccount(request.getSourceAccountId());
        sourceAccount.setAccountStatus(AccountStatus.FROZEN);
        Beneficiary beneficiary = buildBeneficiary(request.getBeneficiaryId(), sourceAccount.getCustomerId());

        when(paymentRepository.findByIdempotencyKey(request.getIdempotencyKey())).thenReturn(Optional.empty());
        when(accountRepository.findByIdForUpdate(request.getSourceAccountId())).thenReturn(Optional.of(sourceAccount));
        when(beneficiaryRepository.findById(request.getBeneficiaryId())).thenReturn(Optional.of(beneficiary));

        assertThatThrownBy(() -> paymentService.processFundTransfer(request))
                .isInstanceOf(InvalidTransferException.class)
                .hasMessageContaining("ACTIVE");
    }

    private FundTransferRequest buildTransferRequest() {
        FundTransferRequest request = new FundTransferRequest();
        request.setSourceAccountId(UUID.randomUUID());
        request.setBeneficiaryId(UUID.randomUUID());
        request.setAmount(new BigDecimal("100.00"));
        request.setIdempotencyKey("idem-key-001");
        return request;
    }

    private Account buildSourceAccount(UUID accountId) {
        Account account = new Account();
        account.setId(accountId);
        account.setCustomerId(UUID.randomUUID());
        account.setAccountNumber("CB1234567890");
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setCurrency("CAD");
        account.setCurrentBalance(new BigDecimal("1000.00"));
        account.setAvailableBalance(new BigDecimal("1000.00"));
        account.setDailyTransferLimit(new BigDecimal("10000.00"));
        return account;
    }

    private Beneficiary buildBeneficiary(UUID beneficiaryId, UUID customerId) {
        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setId(beneficiaryId);
        beneficiary.setCustomerId(customerId);
        beneficiary.setBeneficiaryAccountNumber("9876543210");
        beneficiary.setBeneficiaryName("Jane Doe");
        beneficiary.setStatus(BeneficiaryStatus.ACTIVE);
        return beneficiary;
    }
}
