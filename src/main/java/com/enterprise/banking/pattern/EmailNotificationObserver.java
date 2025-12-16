package com.enterprise.banking.pattern;

import com.enterprise.banking.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Concrete observer implementation for email notifications.
 * Demonstrates Observer pattern for transaction notifications.
 */
@Slf4j
@Component
public class EmailNotificationObserver implements TransactionObserver {

    @Override
    public void onTransactionCompleted(Transaction transaction) {
        log.info("Sending email notification for completed transaction: {} - Amount: {} - Type: {}",
                transaction.getReferenceNumber(),
                transaction.getAmount(),
                transaction.getTransactionType());

        // In real implementation, send email via email service
        sendEmail(
                transaction.getAccount().getUser().getEmail(),
                "Transaction Completed",
                buildCompletionEmailBody(transaction));
    }

    @Override
    public void onTransactionFailed(Transaction transaction, String reason) {
        log.info("Sending email notification for failed transaction: {} - Reason: {}",
                transaction.getReferenceNumber(), reason);

        sendEmail(
                transaction.getAccount().getUser().getEmail(),
                "Transaction Failed",
                buildFailureEmailBody(transaction, reason));
    }

    @Override
    public void onTransactionInitiated(Transaction transaction) {
        log.debug("Transaction initiated notification: {}", transaction.getReferenceNumber());
        // Optional: Send initiation email for large transactions
        if (transaction.getAmount().compareTo(java.math.BigDecimal.valueOf(10000)) > 0) {
            sendEmail(
                    transaction.getAccount().getUser().getEmail(),
                    "Large Transaction Initiated",
                    buildInitiationEmailBody(transaction));
        }
    }

    private void sendEmail(String to, String subject, String body) {
        // Simulated email sending - would integrate with actual email service
        log.info("EMAIL SENT - To: {}, Subject: {}", to, subject);
        log.debug("Email body: {}", body);
    }

    private String buildCompletionEmailBody(Transaction transaction) {
        return String.format(
                "Your transaction has been completed successfully.\n\n" +
                        "Reference Number: %s\n" +
                        "Type: %s\n" +
                        "Amount: $%s\n" +
                        "Balance After: $%s\n" +
                        "Date: %s\n\n" +
                        "Thank you for banking with us!",
                transaction.getReferenceNumber(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getBalanceAfter(),
                transaction.getTransactionDate());
    }

    private String buildFailureEmailBody(Transaction transaction, String reason) {
        return String.format(
                "Your transaction has failed.\n\n" +
                        "Reference Number: %s\n" +
                        "Type: %s\n" +
                        "Amount: $%s\n" +
                        "Reason: %s\n" +
                        "Date: %s\n\n" +
                        "Please contact customer support if you need assistance.",
                transaction.getReferenceNumber(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                reason,
                transaction.getTransactionDate());
    }

    private String buildInitiationEmailBody(Transaction transaction) {
        return String.format(
                "A large transaction has been initiated on your account.\n\n" +
                        "Reference Number: %s\n" +
                        "Type: %s\n" +
                        "Amount: $%s\n" +
                        "Date: %s\n\n" +
                        "If you did not authorize this transaction, please contact us immediately.",
                transaction.getReferenceNumber(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getTransactionDate());
    }
}
