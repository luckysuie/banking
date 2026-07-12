package com.cloudbank.digitalbanking.account.service;

import com.cloudbank.digitalbanking.account.dto.AccountBalanceResponse;
import com.cloudbank.digitalbanking.account.dto.AccountRequest;
import com.cloudbank.digitalbanking.account.dto.AccountResponse;
import com.cloudbank.digitalbanking.account.enums.AccountStatus;

import java.util.List;
import java.util.UUID;

public interface AccountService {

    AccountResponse createAccount(AccountRequest request);

    AccountResponse getAccountById(UUID id);

    AccountResponse getAccountByAccountNumber(String accountNumber);

    List<AccountResponse> getAccountsByCustomerId(UUID customerId);

    AccountBalanceResponse getAccountBalance(UUID id);

    AccountResponse updateAccountStatus(UUID id, AccountStatus accountStatus);
}
