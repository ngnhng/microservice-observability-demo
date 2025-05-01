package org.nguyennn.account_svc.application.dto;

public record AccountBalanceVericationResult(boolean isValid, String message, String transactionId,
        String accountId, String customerId, long amount, String currencyCode) {
}
