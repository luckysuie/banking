package com.cloudbank.digitalbanking.payment.service;

import com.cloudbank.digitalbanking.account.entity.Account;
import com.cloudbank.digitalbanking.account.enums.AccountStatus;
import com.cloudbank.digitalbanking.account.exception.AccountNotFoundException;
import com.cloudbank.digitalbanking.account.repository.AccountRepository;
import com.cloudbank.digitalbanking.audit.constants.AuditResourceType;
import com.cloudbank.digitalbanking.audit.enums.AuditAction;
import com.cloudbank.digitalbanking.audit.enums.AuditResult;
import com.cloudbank.digitalbanking.audit.service.AuditService;
import com.cloudbank.digitalbanking.beneficiary.entity.Beneficiary;
import com.cloudbank.digitalbanking.beneficiary.enums.BeneficiaryStatus;
import com.cloudbank.digitalbanking.beneficiary.exception.BeneficiaryNotFoundException;
import com.cloudbank.digitalbanking.beneficiary.repository.BeneficiaryRepository;
import com.cloudbank.digitalbanking.common.util.MoneyUtils;
import com.cloudbank.digitalbanking.customer.exception.CustomerNotFoundException;
import com.cloudbank.digitalbanking.customer.repository.CustomerRepository;
import com.cloudbank.digitalbanking.notification.service.NotificationService;
import com.cloudbank.digitalbanking.payment.dto.FundTransferRequest;
import com.cloudbank.digitalbanking.payment.dto.PaymentResponse;
import com.cloudbank.digitalbanking.payment.entity.Payment;
import com.cloudbank.digitalbanking.payment.enums.PaymentStatus;
import com.cloudbank.digitalbanking.payment.exception.DuplicatePaymentException;
import com.cloudbank.digitalbanking.payment.exception.InsufficientBalanceException;
import com.cloudbank.digitalbanking.payment.exception.InvalidTransferException;
import com.cloudbank.digitalbanking.payment.exception.PaymentNotFoundException;
import com.cloudbank.digitalbanking.payment.mapper.PaymentMapper;
import com.cloudbank.digitalbanking.payment.repository.PaymentRepository;
import com.cloudbank.digitalbanking.transaction.entity.Transaction;
import com.cloudbank.digitalbanking.transaction.enums.TransactionStatus;
import com.cloudbank.digitalbanking.transaction.enums.TransactionType;
import com.cloudbank.digitalbanking.transaction.mapper.TransactionMapper;
import com.cloudbank.digitalbanking.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private static final List<PaymentStatus> IN_FLIGHT_PAYMENT_STATUSES = List.of(
            PaymentStatus.RECEIVED,
            PaymentStatus.VALIDATING,
            PaymentStatus.COMPLETED);

    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    @Override
    @Transactional
    public PaymentResponse processFundTransfer(FundTransferRequest request) {
        normalizeTransferAmount(request);
        return paymentRepository.findByIdempotencyKey(request.getIdempotencyKey())
                .map(existing -> handleExistingPayment(existing, request))
                .orElseGet(() -> processNewTransfer(request));
    }

    private PaymentResponse processNewTransfer(FundTransferRequest request) {
        try {
            return executeNewTransfer(request);
        } catch (InsufficientBalanceException | InvalidTransferException ex) {
            accountRepository.findById(request.getSourceAccountId())
                    .ifPresent(account -> {
                        notificationService.notifyPaymentFailed(account.getCustomerId(), ex.getMessage());
                        auditService.recordEventInNewTransaction(
                                account.getCustomerId().toString(),
                                AuditAction.PAYMENT_FAILED,
                                AuditResourceType.PAYMENT,
                                request.getSourceAccountId().toString(),
                                "Payment failed: " + ex.getMessage(),
                                AuditResult.FAILURE);
                    });
            throw ex;
        }
    }

    private PaymentResponse handleExistingPayment(Payment existing, FundTransferRequest request) {
        if (matchesRequest(existing, request)) {
            return PaymentMapper.toResponse(existing);
        }
        throw new DuplicatePaymentException(
                "Idempotency key already used with different payment details: " + request.getIdempotencyKey());
    }

    private PaymentResponse executeNewTransfer(FundTransferRequest request) {
        Account sourceAccount = accountRepository.findByIdForUpdate(request.getSourceAccountId())
                .orElseThrow(() -> new AccountNotFoundException(
                        "Source account not found with id: " + request.getSourceAccountId()));

        Beneficiary beneficiary = beneficiaryRepository.findById(request.getBeneficiaryId())
                .orElseThrow(() -> new BeneficiaryNotFoundException(
                        "Beneficiary not found with id: " + request.getBeneficiaryId()));

        validateTransfer(request, sourceAccount, beneficiary);

        Payment payment = savePaymentWithIdempotencyProtection(request, beneficiary, sourceAccount);
        if (payment.getStatus() != PaymentStatus.RECEIVED) {
            return PaymentMapper.toResponse(payment);
        }

        auditService.recordEvent(
                sourceAccount.getCustomerId().toString(),
                AuditAction.PAYMENT_INITIATED,
                AuditResourceType.PAYMENT,
                payment.getId().toString(),
                "Payment initiated with reference " + payment.getPaymentReference(),
                AuditResult.SUCCESS);

        payment.setStatus(PaymentStatus.VALIDATING);
        paymentRepository.save(payment);

        executeTransfer(sourceAccount, beneficiary, payment, request);

        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setCompletedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        notificationService.notifyPaymentSuccess(
                sourceAccount.getCustomerId(),
                payment.getPaymentReference(),
                payment.getAmount(),
                payment.getCurrency());

        auditService.recordEvent(
                sourceAccount.getCustomerId().toString(),
                AuditAction.PAYMENT_COMPLETED,
                AuditResourceType.PAYMENT,
                payment.getId().toString(),
                "Payment completed with reference " + payment.getPaymentReference(),
                AuditResult.SUCCESS);

        return PaymentMapper.toResponse(payment);
    }

    private Payment savePaymentWithIdempotencyProtection(
            FundTransferRequest request,
            Beneficiary beneficiary,
            Account sourceAccount) {
        try {
            return paymentRepository.save(PaymentMapper.toEntity(
                    request,
                    beneficiary.getBeneficiaryAccountNumber(),
                    sourceAccount.getCurrency()));
        } catch (DataIntegrityViolationException ex) {
            return paymentRepository.findByIdempotencyKey(request.getIdempotencyKey())
                    .map(existing -> {
                        if (matchesRequest(existing, request)) {
                            return existing;
                        }
                        throw new DuplicatePaymentException(
                                "Idempotency key already used with different payment details: "
                                        + request.getIdempotencyKey());
                    })
                    .orElseThrow(() -> ex);
        }
    }

    @Override
    public PaymentResponse getPaymentById(UUID id) {
        return PaymentMapper.toResponse(findPaymentOrThrow(id));
    }

    @Override
    public PaymentResponse getPaymentByReference(String paymentReference) {
        Payment payment = paymentRepository.findByPaymentReference(paymentReference)
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found with reference: " + paymentReference));
        return PaymentMapper.toResponse(payment);
    }

    @Override
    public List<PaymentResponse> getPaymentsByCustomerId(UUID customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException("Customer not found with id: " + customerId);
        }
        return paymentRepository.findByCustomerId(customerId).stream()
                .map(PaymentMapper::toResponse)
                .toList();
    }

    private void validateTransfer(FundTransferRequest request, Account sourceAccount, Beneficiary beneficiary) {
        BigDecimal amount = request.getAmount();

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransferException("Transfer amount must be greater than zero");
        }

        if (sourceAccount.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new InvalidTransferException("Source account must be ACTIVE to process transfers");
        }

        if (beneficiary.getStatus() != BeneficiaryStatus.ACTIVE) {
            throw new InvalidTransferException("Beneficiary must be ACTIVE to process transfers");
        }

        if (!beneficiary.getCustomerId().equals(sourceAccount.getCustomerId())) {
            throw new InvalidTransferException("Beneficiary does not belong to the source account customer");
        }

        if (sourceAccount.getAccountNumber().equals(beneficiary.getBeneficiaryAccountNumber())) {
            throw new InvalidTransferException("Source and destination accounts cannot be the same");
        }

        validateDailyTransferLimit(sourceAccount, amount);

        if (sourceAccount.getAvailableBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient available balance. Available: "
                            + sourceAccount.getAvailableBalance() + ", required: " + amount);
        }
    }

    private void validateDailyTransferLimit(Account sourceAccount, BigDecimal transferAmount) {
        LocalDateTime startOfDay = LocalDate.now(ZoneOffset.UTC).atStartOfDay();
        BigDecimal transferredToday = paymentRepository.sumTransferAmountBySourceAccountSince(
                sourceAccount.getId(), IN_FLIGHT_PAYMENT_STATUSES, startOfDay);

        BigDecimal totalAfterTransfer = MoneyUtils.add(transferredToday, transferAmount);
        if (totalAfterTransfer.compareTo(sourceAccount.getDailyTransferLimit()) > 0) {
            throw new InvalidTransferException(
                    "Transfer exceeds daily transfer limit. Limit: "
                            + sourceAccount.getDailyTransferLimit()
                            + ", already transferred today: "
                            + transferredToday);
        }
    }

    private void executeTransfer(
            Account sourceAccount,
            Beneficiary beneficiary,
            Payment payment,
            FundTransferRequest request) {
        BigDecimal amount = request.getAmount();
        String description = buildDescription(request.getDescription(), beneficiary.getBeneficiaryName());
        UUID destinationAccountId = resolveInternalDestinationAccountId(beneficiary.getBeneficiaryAccountNumber());

        debitSourceAccount(sourceAccount, destinationAccountId, payment, amount, description);
        creditDestinationAccountIfInternal(sourceAccount, beneficiary, payment, amount, description);
    }

    private UUID resolveInternalDestinationAccountId(String beneficiaryAccountNumber) {
        return accountRepository.findByAccountNumber(beneficiaryAccountNumber)
                .map(Account::getId)
                .orElse(null);
    }

    private void debitSourceAccount(
            Account sourceAccount,
            UUID relatedAccountId,
            Payment payment,
            BigDecimal amount,
            String description) {
        BigDecimal balanceBefore = MoneyUtils.normalize(sourceAccount.getAvailableBalance());
        BigDecimal balanceAfter = MoneyUtils.subtract(balanceBefore, amount);

        sourceAccount.setCurrentBalance(MoneyUtils.subtract(sourceAccount.getCurrentBalance(), amount));
        sourceAccount.setAvailableBalance(balanceAfter);
        accountRepository.save(sourceAccount);

        Transaction debitTransaction = TransactionMapper.createRecord(
                sourceAccount.getId(),
                relatedAccountId,
                payment.getId(),
                TransactionType.DEBIT,
                amount,
                sourceAccount.getCurrency(),
                description,
                TransactionStatus.COMPLETED,
                balanceBefore,
                balanceAfter);
        transactionRepository.save(debitTransaction);
    }

    private void creditDestinationAccountIfInternal(
            Account sourceAccount,
            Beneficiary beneficiary,
            Payment payment,
            BigDecimal amount,
            String description) {
        accountRepository.findByAccountNumber(beneficiary.getBeneficiaryAccountNumber()).ifPresent(destination -> {
            if (destination.getAccountStatus() != AccountStatus.ACTIVE) {
                throw new InvalidTransferException("Destination account must be ACTIVE to receive transfers");
            }
            if (!destination.getCurrency().equals(sourceAccount.getCurrency())) {
                throw new InvalidTransferException("Destination account currency must match source account currency");
            }

            Account lockedDestination = accountRepository.findByIdForUpdate(destination.getId())
                    .orElseThrow(() -> new AccountNotFoundException(
                            "Destination account not found with id: " + destination.getId()));

            BigDecimal balanceBefore = MoneyUtils.normalize(lockedDestination.getAvailableBalance());
            BigDecimal balanceAfter = MoneyUtils.add(balanceBefore, amount);

            lockedDestination.setCurrentBalance(MoneyUtils.add(lockedDestination.getCurrentBalance(), amount));
            lockedDestination.setAvailableBalance(balanceAfter);
            accountRepository.save(lockedDestination);

            Transaction creditTransaction = TransactionMapper.createRecord(
                    lockedDestination.getId(),
                    sourceAccount.getId(),
                    payment.getId(),
                    TransactionType.CREDIT,
                    amount,
                    lockedDestination.getCurrency(),
                    description,
                    TransactionStatus.COMPLETED,
                    balanceBefore,
                    balanceAfter);
            transactionRepository.save(creditTransaction);
        });
    }

    private String buildDescription(String requestDescription, String beneficiaryName) {
        if (requestDescription != null && !requestDescription.isBlank()) {
            return requestDescription.trim();
        }
        return "Fund transfer to " + beneficiaryName;
    }

    private boolean matchesRequest(Payment existing, FundTransferRequest request) {
        return existing.getSourceAccountId().equals(request.getSourceAccountId())
                && existing.getBeneficiaryId().equals(request.getBeneficiaryId())
                && existing.getAmount().compareTo(request.getAmount()) == 0;
    }

    private Payment findPaymentOrThrow(UUID id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with id: " + id));
    }

    private void normalizeTransferAmount(FundTransferRequest request) {
        request.setAmount(MoneyUtils.normalize(request.getAmount()));
    }
}
