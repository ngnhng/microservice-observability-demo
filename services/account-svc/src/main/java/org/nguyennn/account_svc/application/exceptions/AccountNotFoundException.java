package org.nguyennn.account_svc.application.exceptions;

/**
 * Exception thrown when an account is not found
 */
public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String message) {
        super(message);
    }

    public AccountNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
