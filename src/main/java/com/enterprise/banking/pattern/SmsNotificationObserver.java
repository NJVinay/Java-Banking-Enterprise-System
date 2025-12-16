package com.enterprise.banking.pattern;

import com.enterprise.banking.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Concrete observer implementation for SMS notifications.
 * Demonstrates Observer pattern for transaction notifications.
 */
@Slf4j
@Component
public class SmsNotificationObserver implements TransactionObserver {

    @Override
    public void onTransactionCompleted(Transaction transaction) {
        log.info("Sending SMS notification for completed transaction: {}",
                transaction.getReferenceNumber());

        String phoneNumber = transaction.getAccount().getUser().getPhoneNumber();
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            sendSms(phoneNumber, buildCompletionSmsMessage(transaction));
        }
    }

    @Override
    public void onTransactionFailed(Transaction transaction, String reason) {
        log.info("Sending SMS notification for failed transaction: {}",
                transaction.getReferenceNumber());

        String phoneNumber = transaction.getAccount().getUser().getPhoneNumber();
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            sendSms(phoneNumber, buildFailureSmsMessage(transaction, reason));
        }
    }

    @Override
    public void onTransactionInitiated(Transaction transaction) {
        log.debug("Transaction initiated SMS notification: {}",
                transaction.getReferenceNumber());
        // SMS notifications typically sent only on completion/failure to reduce costs
    }

    private void sendSms(String phoneNumber, String message) {
        // Simulated SMS sending - would integrate with actual SMS service (Twilio, AWS
        // SNS, etc.)
        log.info("SMS SENT - To: {}, Message: {}", phoneNumber, message);
    }

    private String buildCompletionSmsMessage(Transaction transaction) {
        return String.format(
                "Transaction completed. Ref: %s, Amount: $%s, Balance: $%s",
                transaction.getReferenceNumber().substring(0, 8),
                transaction.getAmount(),
                transaction.getBalanceAfter());
    }

    private String buildFailureSmsMessage(Transaction transaction, String reason) {
        return String.format(
                "Transaction failed. Ref: %s, Amount: $%s, Reason: %s",
                transaction.getReferenceNumber().substring(0, 8),
                transaction.getAmount(),
                reason.length() > 50 ? reason.substring(0, 50) : reason);
    }
}
