package org.nguyennn.account_svc.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.nguyennn.account_svc.domain.Account;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "accounts")
public class AccountEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "account_id")
    private UUID id;

    @NotNull
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @NotNull
    @Column(name = "account_number", nullable = false, unique = true)
    @Size(min = 5, max = 50)
    private String accountNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, columnDefinition = "account_type_enum")
    private AccountType type;

    @NotNull
    @Pattern(regexp = "^[A-Z]{3}$")
    @Size(min = 3, max = 3)
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @NotNull
    @PositiveOrZero
    @Column(name = "balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "account_status_enum")
    private AccountStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    public Account toDomain() {
        return Account.builder()
                .id(this.id)
                .customerId(this.customerId)
                .accountNumber(this.accountNumber)
                .type(Account.AccountType.valueOf(this.type.name()))
                .balance(this.balance)
                .currency(this.currency)
                .status(Account.AccountStatus.valueOf(this.status.name()))
                .createdAt(this.createdAt != null ? this.createdAt.toInstant() : null)
                .updatedAt(this.updatedAt != null ? this.updatedAt.toInstant() : null)
                .build();
    }

    public static AccountEntity fromDomain(Account account) {
        AccountEntity entity = new AccountEntity();
        entity.id = account.getId();
        entity.customerId = account.getCustomerId();
        entity.accountNumber = account.getAccountNumber();
        entity.type = AccountType.valueOf(account.getType().name());
        entity.balance = account.getBalance();
        entity.currency = account.getCurrency();
        entity.status = AccountStatus.valueOf(account.getStatus().name());
        entity.createdAt = ZonedDateTime.now();
        entity.updatedAt = ZonedDateTime.now();
        return entity;
    }

    // Enum types matching the database schema
    public enum AccountType {
        CHECKING, SAVINGS, LOAN, MERCHANT
    }

    public enum AccountStatus {
        ACTIVE, DORMANT, FROZEN, CLOSED
    }

    public UUID getId() {
        return id;
    }
}
