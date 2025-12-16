package com.enterprise.banking.pattern;

import com.enterprise.banking.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Observable subject for the Observer pattern.
 * Manages transaction observers and notifies them of transaction events.
 */
@Slf4j
@Component
public class TransactionNotifier {

    private final List<TransactionObserver> observers = new CopyOnWriteArrayList<>();

    /**
     * Register an observer to receive transaction notifications.
     */
    public void registerObserver(TransactionObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
            log.info("Registered observer: {}", observer.getClass().getSimpleName());
        }
    }

    /**
     * Unregister an observer from receiving notifications.
     */
    public void unregisterObserver(TransactionObserver observer) {
        observers.remove(observer);
        log.info("Unregistered observer: {}", observer.getClass().getSimpleName());
    }

    /**
     * Notify all observers that a transaction has been initiated.
     */
    public void notifyTransactionInitiated(Transaction transaction) {
        log.debug("Notifying {} observers about transaction initiation: {}",
                observers.size(), transaction.getReferenceNumber());

        observers.forEach(observer -> {
            try {
                observer.onTransactionInitiated(transaction);
            } catch (Exception e) {
                log.error("Error notifying observer about transaction initiation", e);
            }
        });
    }

    /**
     * Notify all observers that a transaction has been completed.
     */
    public void notifyTransactionCompleted(Transaction transaction) {
        log.debug("Notifying {} observers about transaction completion: {}",
                observers.size(), transaction.getReferenceNumber());

        observers.forEach(observer -> {
            try {
                observer.onTransactionCompleted(transaction);
            } catch (Exception e) {
                log.error("Error notifying observer about transaction completion", e);
            }
        });
    }

    /**
     * Notify all observers that a transaction has failed.
     */
    public void notifyTransactionFailed(Transaction transaction, String reason) {
        log.debug("Notifying {} observers about transaction failure: {} - Reason: {}",
                observers.size(), transaction.getReferenceNumber(), reason);

        observers.forEach(observer -> {
            try {
                observer.onTransactionFailed(transaction, reason);
            } catch (Exception e) {
                log.error("Error notifying observer about transaction failure", e);
            }
        });
    }

    /**
     * Get count of registered observers.
     */
    public int getObserverCount() {
        return observers.size();
    }

    /**
     * Clear all registered observers.
     */
    public void clearObservers() {
        observers.clear();
        log.info("Cleared all observers");
    }
}
