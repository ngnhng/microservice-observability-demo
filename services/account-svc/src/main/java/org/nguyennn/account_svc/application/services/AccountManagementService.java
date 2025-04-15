package org.nguyennn.account_svc.application.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.nguyennn.account_svc.adapter.in.api.Paginated;
import org.nguyennn.account_svc.application.dto.CustomerAccounts;
import org.nguyennn.account_svc.application.exceptions.AccountNotFoundException;
import org.nguyennn.account_svc.application.in.AccountUseCases;
import org.nguyennn.account_svc.application.out.AccountPersistencePort;
import org.nguyennn.account_svc.domain.Account;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@ApplicationScoped
public class AccountManagementService implements AccountUseCases {

    private final AccountPersistencePort accountPersistencePort;

    @Inject
    public AccountManagementService(AccountPersistencePort accountPersistencePort) {
        this.accountPersistencePort = accountPersistencePort;
    }

    @Override
    public Account createAccount(@NotNull @Valid Account account) {
        return accountPersistencePort.saveAccount(account);
    }

    @Override
    public Optional<Account> getAccountById(@NotNull UUID accountId) {
        return accountPersistencePort.findById(accountId);
    }

    @Override
    public Account getAccountByNumber(@NotNull String accountNumber) {
        return accountPersistencePort.findByAccountNumber(accountNumber);
    }

    @Override
    public CustomerAccounts getAccountsByCustomerId(
            @NotNull UUID customerId,
            @PositiveOrZero int offset,
            @Min(value = 1) int limit) {
        return accountPersistencePort.findByCustomerId(customerId, offset, limit);
    }

    @Override
    public List<Account> getAccountsByStatus(@NotNull Account.AccountStatus status) {
        return accountPersistencePort.findByStatus(status);
    }

    @Override
    public Account updateAccount(@NotNull @Valid Account account) {
        Optional<Account> existingAccount = accountPersistencePort.findById(account.getId());
        if (existingAccount.isEmpty()) {
            throw new AccountNotFoundException("Account not found with ID: " + account.getId());
        }
        return accountPersistencePort.saveAccount(account);
    }

    @Override
    public void deleteAccount(@NotNull UUID accountId) {
        accountPersistencePort.deleteAccount(accountId);
    }
}
