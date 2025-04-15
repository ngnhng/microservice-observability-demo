package org.nguyennn.account_svc.application.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.nguyennn.account_svc.application.dto.CustomerAccounts;
import org.nguyennn.account_svc.domain.Account;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Port for account persistence operations
 */
public interface AccountPersistencePort {

    /**
     * Save or update an account
     *
     * @param account the account to save
     * @return the saved account
     */
    Account saveAccount(@NotNull @Valid Account account);

    /**
     * Find an account by ID
     *
     * @param id the account ID
     * @return the account if found
     */
    Optional<Account> findById(@NotNull UUID id);

    /**
     * Find an account by account number
     *
     * @param accountNumber the account number
     * @return the account if found
     */
    Account findByAccountNumber(@NotNull String accountNumber);

    /**
     * Find accounts by customer ID with pagination
     *
     * @param customerId the customer ID
     * @param offset     starting position
     * @param limit      maximum number of accounts to return
     * @return a customer's list of accounts
     */
    CustomerAccounts findByCustomerId(@NotNull UUID customerId, int offset, int limit);

    /**
     * Find accounts with a specific status
     *
     * @param status the account status
     * @return list of accounts with the specified status
     */
    List<Account> findByStatus(@NotNull Account.AccountStatus status);

    /**
     * Delete an account by ID
     *
     * @param id the account ID
     */
    void deleteAccount(@NotNull UUID id);
}