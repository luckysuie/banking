package com.cloudbank.digitalbanking.account.mapper;

import com.cloudbank.digitalbanking.account.dto.AccountBalanceResponse;
import com.cloudbank.digitalbanking.account.dto.AccountRequest;
import com.cloudbank.digitalbanking.account.dto.AccountResponse;
import com.cloudbank.digitalbanking.account.entity.Account;
import com.cloudbank.digitalbanking.account.enums.AccountStatus;
import com.cloudbank.digitalbanking.common.constants.BankingConstants;
import com.cloudbank.digitalbanking.common.util.ReferenceGenerator;

import java.math.BigDecimal;

public final class AccountMapper {

    private AccountMapper() {
    }

    public static Account toEntity(AccountRequest request) {
        Account account = new Account();
        account.setAccountNumber(ReferenceGenerator.generateAccountNumber());
        account.setCustomerId(request.getCustomerId());
        account.setAccountType(request.getAccountType());
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setCurrency(BankingConstants.DEFAULT_ACCOUNT_CURRENCY);
        account.setCurrentBalance(BigDecimal.ZERO);
        account.setAvailableBalance(BigDecimal.ZERO);
        account.setDailyTransferLimit(resolveDailyTransferLimit(request.getDailyTransferLimit()));
        return account;
    }

    public static AccountResponse toResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .customerId(account.getCustomerId())
                .accountType(account.getAccountType())
                .accountStatus(account.getAccountStatus())
                .currency(account.getCurrency())
                .currentBalance(account.getCurrentBalance())
                .availableBalance(account.getAvailableBalance())
                .dailyTransferLimit(account.getDailyTransferLimit())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    public static AccountBalanceResponse toBalanceResponse(Account account) {
        return AccountBalanceResponse.builder()
                .accountId(account.getId())
                .accountNumber(account.getAccountNumber())
                .currency(account.getCurrency())
                .currentBalance(account.getCurrentBalance())
                .availableBalance(account.getAvailableBalance())
                .build();
    }

    private static BigDecimal resolveDailyTransferLimit(BigDecimal dailyTransferLimit) {
        return dailyTransferLimit != null
                ? dailyTransferLimit
                : BankingConstants.DEFAULT_DAILY_TRANSFER_LIMIT;
    }
}
