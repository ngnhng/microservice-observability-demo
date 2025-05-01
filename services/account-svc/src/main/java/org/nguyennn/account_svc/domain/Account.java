package org.nguyennn.account_svc.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class Account {
    @NotNull(message = "Account ID cannot be null")
    private final UUID id;

    @NotNull(message = "Customer ID cannot be null")
    private final UUID customerId;

    @NotNull(message = "Account number is required")
    @Size(min = 5, max = 50, message = "Account number must be between 5 and 50 characters")
    private final String accountNumber;

    @NotNull(message = "Account type is required")
    private final AccountType type;

    @NotNull(message = "Balance cannot be null")
    private final BigDecimal balance;

    @NotNull(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter ISO currency code")
    @Size(min = 3, max = 3)
    private final String currency;

    @NotNull(message = "Status cannot be null")
    private AccountStatus status;

    private final Instant createdAt;

    private Instant updatedAt;

    private Account(Builder builder) {
        this.id = builder.id;
        this.customerId = builder.customerId;
        this.accountNumber = builder.accountNumber;
        this.type = builder.type;
        this.balance = builder.balance;
        this.currency = builder.currency;
        this.status = builder.status;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public enum AccountType {
        CHECKING, SAVINGS, LOAN, MERCHANT
    }

    public enum AccountStatus {
        ACTIVE, DORMANT, FROZEN, CLOSED
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public AccountType getType() {
        return type;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getCurrency() {
        return currency;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID customerId;
        private String accountNumber;
        private AccountType type;
        private BigDecimal balance;
        private String currency;
        private AccountStatus status;
        private Instant createdAt;
        private Instant updatedAt;

        private Builder() {
        }

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder customerId(UUID customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder accountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
            return this;
        }

        public Builder type(AccountType type) {
            this.type = type;
            return this;
        }

        public Builder balance(BigDecimal balance) {
            this.balance = balance;
            return this;
        }

        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder status(AccountStatus status) {
            this.status = status;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Account build() {
            return new Account(this);
        }
    }
}
