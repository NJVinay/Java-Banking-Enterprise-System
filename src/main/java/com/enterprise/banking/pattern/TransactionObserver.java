package com.enterprise.banking.pattern;

import com.enterprise.banking.model.Transaction;

/**
 * Observer interface for the Observer pattern.
 * Observers receive notifications about transaction events.
 */
public interface TransactionObserver {

    /**
     * Called when a transaction is completed.
     */
    void onTransactionCompleted(Transaction transaction);

    /**
     * Called when a transaction fails.
     */
    void onTransactionFailed(Transaction transaction, String reason);

    /**
     * Called when a transaction is initiated.
     */
    void onTransactionInitiated(Transaction transaction);
}
