package com.cloudbank.digitalbanking.account.service;

import com.cloudbank.digitalbanking.account.dto.AccountBalanceResponse;
import com.cloudbank.digitalbanking.account.dto.AccountRequest;
import com.cloudbank.digitalbanking.account.dto.AccountResponse;
import com.cloudbank.digitalbanking.account.entity.Account;
import com.cloudbank.digitalbanking.account.enums.AccountStatus;
import com.cloudbank.digitalbanking.account.exception.AccountNotFoundException;
import com.cloudbank.digitalbanking.account.exception.InvalidAccountStatusException;
import com.cloudbank.digitalbanking.account.mapper.AccountMapper;
import com.cloudbank.digitalbanking.account.repository.AccountRepository;
import com.cloudbank.digitalbanking.audit.constants.AuditResourceType;
import com.cloudbank.digitalbanking.audit.enums.AuditAction;
import com.cloudbank.digitalbanking.audit.enums.AuditResult;
import com.cloudbank.digitalbanking.audit.service.AuditService;
import com.cloudbank.digitalbanking.customer.exception.CustomerNotFoundException;
import com.cloudbank.digitalbanking.customer.repository.CustomerRepository;
import com.cloudbank.digitalbanking.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    @Override
    @Transactional
    public AccountResponse createAccount(AccountRequest request) {
        validateCustomerExists(request.getCustomerId());
        Account account = accountRepository.save(AccountMapper.toEntity(request));
        notificationService.notifyAccountCreated(request.getCustomerId(), account.getAccountNumber());
        auditService.recordEvent(
                request.getCustomerId().toString(),
                AuditAction.ACCOUNT_CREATED,
                AuditResourceType.ACCOUNT,
                account.getId().toString(),
                "Account created with number " + account.getAccountNumber(),
                AuditResult.SUCCESS);
        return AccountMapper.toResponse(account);
    }

    @Override
    public AccountResponse getAccountById(UUID id) {
        return AccountMapper.toResponse(findAccountOrThrow(id));
    }

    @Override
    public AccountResponse getAccountByAccountNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found with account number: " + accountNumber));
        return AccountMapper.toResponse(account);
    }

    @Override
    public List<AccountResponse> getAccountsByCustomerId(UUID customerId) {
        validateCustomerExists(customerId);
        return accountRepository.findByCustomerId(customerId).stream()
                .map(AccountMapper::toResponse)
                .toList();
    }

    @Override
    public AccountBalanceResponse getAccountBalance(UUID id) {
        return AccountMapper.toBalanceResponse(findAccountOrThrow(id));
    }

    @Override
    @Transactional
    public AccountResponse updateAccountStatus(UUID id, AccountStatus accountStatus) {
        Account account = findAccountOrThrow(id);
        validateStatusChange(account.getAccountStatus(), accountStatus);
        account.setAccountStatus(accountStatus);
        auditService.recordEvent(
                account.getCustomerId().toString(),
                AuditAction.ACCOUNT_STATUS_CHANGED,
                AuditResourceType.ACCOUNT,
                account.getId().toString(),
                "Account status changed to " + accountStatus,
                AuditResult.SUCCESS);
        return AccountMapper.toResponse(account);
    }

    private Account findAccountOrThrow(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + id));
    }

    private void validateCustomerExists(UUID customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException("Customer not found with id: " + customerId);
        }
    }

    private void validateStatusChange(AccountStatus currentStatus, AccountStatus newStatus) {
        if (currentStatus == AccountStatus.CLOSED) {
            throw new InvalidAccountStatusException("Cannot change status of a closed account");
        }
        if (newStatus == null) {
            throw new InvalidAccountStatusException("Account status is required");
        }
    }
}
