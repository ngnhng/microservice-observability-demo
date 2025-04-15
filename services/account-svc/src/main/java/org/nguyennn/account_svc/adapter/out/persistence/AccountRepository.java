package org.nguyennn.account_svc.adapter.out.persistence;

import java.util.List;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.nguyennn.account_svc.domain.Account;

@ApplicationScoped
public class AccountRepository implements PanacheRepositoryBase<AccountEntity, UUID> {

    public AccountEntity findByAccountNumber(String accountNumber) {
        return find("accountNumber", accountNumber)
                .firstResult();
    }

    public AccountEntity save(AccountEntity entity) {
        if (entity.isPersistent()) {
            entity.persist();
        } else {
            entity.persistAndFlush();
        }
        return entity;
    }

    public List<AccountEntity> findByCustomerId(UUID customerId, int offset, int limit) {
        return find("customerId", customerId)
                .page(offset, limit)
                .list();
    }

    public long countByCustomerId(UUID customerId) {
        return count("customerId", customerId);
    }

    public List<AccountEntity> findByStatus(Account.AccountStatus status) {
        return find("status", status)
                .list();
    }
}