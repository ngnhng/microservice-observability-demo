package org.nguyennn.account_svc.application.in;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.nguyennn.account_svc.application.dto.CustomerAccounts;
import org.nguyennn.account_svc.domain.Account;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Interface defining the use cases for account operations
 */
public interface AccountUseCases {

    /**
     * Create a new account
     *
     * @param account the account to create
     * @return the created account
     */
    Account createAccount(@NotNull @Valid Account account);

    /**
     * Get an account by ID
     *
     * @param accountId the account ID
     * @return the account if found
     */
    Optional<Account> getAccountById(@NotNull UUID accountId);

    /**
     * Get an account by account number
     *
     * @param accountNumber the account number
     * @return the account if found
     */
    Account getAccountByNumber(@NotNull String accountNumber);

    /**
     * Get a paginated list of accounts for a specific customer
     *
     * @param customerId the customer ID
     * @param offset     the starting position for pagination
     * @param limit      the maximum number of items to return
     * @return customer accounts with pagination info
     */
    CustomerAccounts getAccountsByCustomerId(
            @NotNull UUID customerId,
            @PositiveOrZero int offset,
            @Min(value = 1) int limit);

    /**
     * Get all accounts with a specific status
     *
     * @param status the account status
     * @return list of accounts with the specified status
     */
    List<Account> getAccountsByStatus(@NotNull Account.AccountStatus status);

    /**
     * Update an existing account
     *
     * @param account the account to update
     * @return the updated account
     */
    Account updateAccount(@NotNull @Valid Account account);

    /**
     * Delete an account
     *
     * @param accountId the ID of the account to delete
     */
    void deleteAccount(@NotNull UUID accountId);
}
