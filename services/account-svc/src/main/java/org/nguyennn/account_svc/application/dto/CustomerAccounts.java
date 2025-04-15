package org.nguyennn.account_svc.application.dto;

import org.nguyennn.account_svc.domain.Account;

import java.util.List;
import java.util.UUID;

public record CustomerAccounts(
        UUID customerId,
        long count,
        List<Account> accounts
) {
}
