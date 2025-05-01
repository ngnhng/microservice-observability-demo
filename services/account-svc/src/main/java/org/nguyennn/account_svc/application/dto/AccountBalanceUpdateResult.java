package org.nguyennn.account_svc.application.dto;

import java.math.BigDecimal;

public record AccountBalanceUpdateResult(BigDecimal oldBalance, BigDecimal newBalance,
        String transactionId, String accountId, String customerId, String currencyCode) {
}
