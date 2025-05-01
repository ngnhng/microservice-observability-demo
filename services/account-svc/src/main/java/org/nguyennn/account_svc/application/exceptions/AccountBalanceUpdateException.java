package org.nguyennn.account_svc.application.exceptions;

/**
 * AccountBalanceUpdateException is thrown when there is an error updating the account balance.
 */
public class AccountBalanceUpdateException extends RuntimeException {
    public AccountBalanceUpdateException(String message) {
        super(message);
    }

    public AccountBalanceUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
