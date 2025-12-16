package com.enterprise.banking.exception;

/**
 * Exception thrown when account is not found.
 */
public class AccountNotFoundException extends BankingException {

    public AccountNotFoundException(String accountNumber) {
        super("Account not found: " + accountNumber);
    }
}
