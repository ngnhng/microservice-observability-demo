package org.nguyennn.account_svc.application.dto;

import java.util.List;
import java.util.UUID;

import org.nguyennn.account_svc.domain.Account;

public record CustomerAccounts(UUID customerId, long count, List<Account> accounts) {
}
