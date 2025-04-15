package org.nguyennn.account_svc.adapter.in.api;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import jakarta.transaction.Transactional;
import org.jboss.resteasy.reactive.RestPath;
import org.nguyennn.account_svc.application.exceptions.AccountNotFoundException;
import org.nguyennn.account_svc.application.in.AccountUseCases;
import org.nguyennn.account_svc.domain.Account;
import org.nguyennn.account_svc.domain.Account.AccountStatus;

import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/v1")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AccountResource {

    private final AccountUseCases accountUseCases;

    @Inject
    public AccountResource(AccountUseCases accountUseCases) {
        this.accountUseCases = accountUseCases;
    }

    @GET
    @Path("/accounts")
    @RunOnVirtualThread
    public Response getAccountByNumber(
            @NotNull(message = "Account number cannot be null")
            @QueryParam("accountNumber") String accountNumber) {

        Account account = accountUseCases.getAccountByNumber(accountNumber);
        if (account != null) {
            return Response.ok(ApiResponse.success("Account retrieved successfully", account)).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("Account not found with number: " + accountNumber))
                    .build();
        }
    }

    @GET
    @Path("/accounts/{accountId}")
    @RunOnVirtualThread
    public Response getAccountById(
            @RestPath
            @NotNull(message = "Account ID cannot be null") UUID accountId) {

        Optional<Account> account = accountUseCases.getAccountById(accountId);
        if (account.isPresent()) {
            return Response.ok(ApiResponse.success("Account retrieved successfully", account.get())).build();
        } else {
            throw new AccountNotFoundException("Account not found with ID: " + accountId);
        }
    }

    /**
     * Get the balance of a specific account
     */
    @GET
    @Path("/accounts/{accountId}/balance")
    @RunOnVirtualThread
    public Response getAccountBalance(
            @RestPath
            @NotNull(message = "Account ID cannot be null") UUID accountId) {

        Optional<Account> accountOpt = accountUseCases.getAccountById(accountId);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            Map<String, Object> balanceInfo = Map.of(
                    "accountId", account.getId(),
                    "balance", account.getBalance(),
                    "currency", account.getCurrency()
            );
            return Response.ok(ApiResponse.success("Balance retrieved successfully", balanceInfo)).build();
        } else {
            throw new AccountNotFoundException("Account not found with ID: " + accountId);
        }
    }

    /**
     * Update the status of a specific account
     */
    @PATCH
    @Path("/accounts/{accountId}/status")
    @RunOnVirtualThread
    @Transactional
    public Response updateAccountStatus(
            @RestPath
            @NotNull(message = "Account ID cannot be null") UUID accountId,
            @NotNull(message = "Status update body cannot be null") @Valid Map<String, String> statusUpdate) {

        if (!statusUpdate.containsKey("status")) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Status field is required in the request body"))
                    .build();
        }

        String statusStr = statusUpdate.get("status");
        AccountStatus status;
        try {
            status = AccountStatus.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Invalid status value. Allowed values: ACTIVE, DORMANT, FROZEN, CLOSED"))
                    .build();
        }

        Optional<Account> accountOpt = accountUseCases.getAccountById(accountId);
        if (accountOpt.isEmpty()) {
            throw new AccountNotFoundException("Account not found with ID: " + accountId);
        }

        Account account = accountOpt.get();
        account.setStatus(status);
        Account updatedAccount = accountUseCases.updateAccount(account);

        return Response.ok(ApiResponse.success("Account status updated successfully", updatedAccount)).build();
    }
}
