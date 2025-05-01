package org.nguyennn.account_svc.adapter.out.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.nguyennn.account_svc.application.dto.CustomerAccounts;
import org.nguyennn.account_svc.application.out.persistence.AccountPersistencePort;
import org.nguyennn.account_svc.domain.Account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class AccountPersistenceImpl implements AccountPersistencePort {

    @Inject
    private AccountRepository accountRepository;


    @Override
    public Account saveAccount(Account account) {
        AccountEntity entity = AccountEntity.fromDomain(account);
        AccountEntity savedEntity = accountRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Account> findById(UUID id) {
        AccountEntity entity = accountRepository.findById(id);
        return entity != null ? Optional.of(entity.toDomain()) : Optional.empty();
    }

    @Override
    public Account findByAccountNumber(String accountNumber) {
        AccountEntity entity = accountRepository.findByAccountNumber(accountNumber);
        return entity != null ? entity.toDomain() : null;
    }

    @Override
    public CustomerAccounts findByCustomerId(UUID customerId, int offset, int limit) {
        List<AccountEntity> entities =
                accountRepository.findByCustomerId(customerId, offset, limit);
        long totalCount = accountRepository.countByCustomerId(customerId);

        List<Account> accounts = entities.stream().map(AccountEntity::toDomain).toList();

        return new CustomerAccounts(customerId, totalCount, accounts);
    }

    @Override
    public List<Account> findByStatus(Account.AccountStatus status) {
        List<AccountEntity> entities = accountRepository.findByStatus(status);
        return entities.stream().map(AccountEntity::toDomain).toList();
    }

    @Override
    public void deleteAccount(UUID id) {
        accountRepository.deleteById(id);
    }
}
