package com.enterprise.banking.exception;

/**
 * Exception thrown when there are insufficient funds for a transaction.
 */
public class InsufficientFundsException extends BankingException {

    public InsufficientFundsException(String message) {
        super(message);
    }
}
