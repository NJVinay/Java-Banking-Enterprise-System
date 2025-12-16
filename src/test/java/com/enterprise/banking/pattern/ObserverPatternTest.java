package com.enterprise.banking.pattern;

import com.enterprise.banking.model.Account;
import com.enterprise.banking.model.Transaction;
import com.enterprise.banking.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for Observer Pattern (TransactionNotifier and Observers).
 */
class ObserverPatternTest {
    
    private TransactionNotifier notifier;
    private EmailNotificationObserver emailObserver;
    private SmsNotificationObserver smsObserver;
    private Transaction testTransaction;
    
    @BeforeEach
    void setUp() {
        notifier = new TransactionNotifier();
        emailObserver = new EmailNotificationObserver();
        smsObserver = new SmsNotificationObserver();
        
        User user = new User();
        user.setEmail("test@example.com");
        user.setPhoneNumber("+1234567890");
        
        Account account = new Account();
        account.setAccountNumber("CHK-123456-ABCD");
        account.setUser(user);
        
        testTransaction = new Transaction();
        testTransaction.setReferenceNumber("TXN-123456-ABCD");
        testTransaction.setTransactionType(Transaction.TransactionType.DEPOSIT);
        testTransaction.setAmount(new BigDecimal("500.00"));
        testTransaction.setBalanceAfter(new BigDecimal("1500.00"));
        testTransaction.setAccount(account);
    }
    
    @Test
    void testRegisterObserver() {
        // Act
        notifier.registerObserver(emailObserver);
        notifier.registerObserver(smsObserver);
        
        // Assert
        assertEquals(2, notifier.getObserverCount());
    }
    
    @Test
    void testUnregisterObserver() {
        // Arrange
        notifier.registerObserver(emailObserver);
        notifier.registerObserver(smsObserver);
        
        // Act
        notifier.unregisterObserver(emailObserver);
        
        // Assert
        assertEquals(1, notifier.getObserverCount());
    }
    
    @Test
    void testNotifyTransactionCompleted() {
        // Arrange
        notifier.registerObserver(emailObserver);
        notifier.registerObserver(smsObserver);
        
        // Act & Assert (should not throw exception)
        assertDoesNotThrow(() -> {
            notifier.notifyTransactionCompleted(testTransaction);
        });
    }
    
    @Test
    void testNotifyTransactionFailed() {
        // Arrange
        notifier.registerObserver(emailObserver);
        notifier.registerObserver(smsObserver);
        
        // Act & Assert (should not throw exception)
        assertDoesNotThrow(() -> {
            notifier.notifyTransactionFailed(testTransaction, "Insufficient funds");
        });
    }
    
    @Test
    void testNotifyTransactionInitiated() {
        // Arrange
        notifier.registerObserver(emailObserver);
        
        // Act & Assert (should not throw exception)
        assertDoesNotThrow(() -> {
            notifier.notifyTransactionInitiated(testTransaction);
        });
    }
    
    @Test
    void testClearObservers() {
        // Arrange
        notifier.registerObserver(emailObserver);
        notifier.registerObserver(smsObserver);
        
        // Act
        notifier.clearObservers();
        
        // Assert
        assertEquals(0, notifier.getObserverCount());
    }
    
    @Test
    void testRegisterDuplicateObserver() {
        // Arrange
        notifier.registerObserver(emailObserver);
        
        // Act
        notifier.registerObserver(emailObserver); // Register again
        
        // Assert
        assertEquals(1, notifier.getObserverCount()); // Should not add duplicate
    }
}
