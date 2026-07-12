package com.cloudbank.digitalbanking.account.service;

import com.cloudbank.digitalbanking.account.dto.AccountRequest;
import com.cloudbank.digitalbanking.account.dto.AccountResponse;
import com.cloudbank.digitalbanking.account.entity.Account;
import com.cloudbank.digitalbanking.account.enums.AccountType;
import com.cloudbank.digitalbanking.account.exception.AccountNotFoundException;
import com.cloudbank.digitalbanking.account.repository.AccountRepository;
import com.cloudbank.digitalbanking.customer.exception.CustomerNotFoundException;
import com.cloudbank.digitalbanking.customer.repository.CustomerRepository;
import com.cloudbank.digitalbanking.notification.service.NotificationService;
import com.cloudbank.digitalbanking.audit.service.AuditService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    void createAccount_shouldReturnAccountResponse_whenCustomerExists() {
        UUID customerId = UUID.randomUUID();
        AccountRequest request = new AccountRequest();
        request.setCustomerId(customerId);
        request.setAccountType(AccountType.CHEQUING);

        Account savedAccount = new Account();
        savedAccount.setId(UUID.randomUUID());
        savedAccount.setAccountNumber("CB1234567890");
        savedAccount.setCustomerId(customerId);
        savedAccount.setAccountType(AccountType.CHEQUING);

        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        AccountResponse response = accountService.createAccount(request);

        assertThat(response.getCustomerId()).isEqualTo(customerId);
        assertThat(response.getAccountType()).isEqualTo(AccountType.CHEQUING);
        verify(accountRepository).save(any(Account.class));
        verify(notificationService).notifyAccountCreated(customerId, savedAccount.getAccountNumber());
        verify(auditService).recordEvent(
                org.mockito.ArgumentMatchers.eq(customerId.toString()),
                org.mockito.ArgumentMatchers.eq(com.cloudbank.digitalbanking.audit.enums.AuditAction.ACCOUNT_CREATED),
                org.mockito.ArgumentMatchers.eq(com.cloudbank.digitalbanking.audit.constants.AuditResourceType.ACCOUNT),
                org.mockito.ArgumentMatchers.any(String.class),
                org.mockito.ArgumentMatchers.contains("Account created"),
                org.mockito.ArgumentMatchers.eq(com.cloudbank.digitalbanking.audit.enums.AuditResult.SUCCESS));
    }

    @Test
    void createAccount_shouldThrowCustomerNotFoundException_whenCustomerDoesNotExist() {
        UUID customerId = UUID.randomUUID();
        AccountRequest request = new AccountRequest();
        request.setCustomerId(customerId);
        request.setAccountType(AccountType.SAVINGS);

        when(customerRepository.existsById(customerId)).thenReturn(false);

        assertThatThrownBy(() -> accountService.createAccount(request))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining(customerId.toString());

        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void getAccountsByCustomerId_shouldReturnAllAccountsForCustomer() {
        UUID customerId = UUID.randomUUID();
        Account chequing = new Account();
        chequing.setCustomerId(customerId);
        chequing.setAccountType(AccountType.CHEQUING);

        Account savings = new Account();
        savings.setCustomerId(customerId);
        savings.setAccountType(AccountType.SAVINGS);

        when(customerRepository.existsById(customerId)).thenReturn(true);
        when(accountRepository.findByCustomerId(customerId)).thenReturn(java.util.List.of(chequing, savings));

        var responses = accountService.getAccountsByCustomerId(customerId);

        assertThat(responses).hasSize(2);
        assertThat(responses)
                .extracting(AccountResponse::getAccountType)
                .containsExactlyInAnyOrder(AccountType.CHEQUING, AccountType.SAVINGS);
    }

    @Test
    void getAccountById_shouldThrowAccountNotFoundException_whenAccountDoesNotExist() {
        UUID accountId = UUID.randomUUID();
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccountById(accountId))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining(accountId.toString());
    }
}
