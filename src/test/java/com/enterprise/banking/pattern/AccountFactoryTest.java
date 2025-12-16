package com.enterprise.banking.pattern;

import com.enterprise.banking.model.Account;
import com.enterprise.banking.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit tests for AccountFactory (Factory Pattern).
 */
class AccountFactoryTest {
    
    private AccountFactory accountFactory;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        accountFactory = new AccountFactory();
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
    }
    
    @Test
    void testCreateCheckingAccount() {
        // Act
        Account account = accountFactory.createAccount(Account.AccountType.CHECKING, testUser);
        
        // Assert
        assertNotNull(account);
        assertEquals(Account.AccountType.CHECKING, account.getAccountType());
        assertTrue(account.getAccountNumber().startsWith("CHK-"));
        assertEquals(BigDecimal.valueOf(0.01), account.getInterestRate());
        assertEquals(new BigDecimal("500.00"), account.getOverdraftLimit());
        assertNull(account.getCreditLimit());
        assertEquals(Account.AccountStatus.ACTIVE, account.getStatus());
    }
    
    @Test
    void testCreateSavingsAccount() {
        // Act
        Account account = accountFactory.createAccount(Account.AccountType.SAVINGS, testUser);
        
        // Assert
        assertNotNull(account);
        assertEquals(Account.AccountType.SAVINGS, account.getAccountType());
        assertTrue(account.getAccountNumber().startsWith("SAV-"));
        assertEquals(BigDecimal.valueOf(0.02), account.getInterestRate());
        assertEquals(BigDecimal.ZERO, account.getOverdraftLimit());
        assertNull(account.getCreditLimit());
    }
    
    @Test
    void testCreateCreditAccount() {
        // Act
        Account account = accountFactory.createAccount(Account.AccountType.CREDIT, testUser);
        
        // Assert
        assertNotNull(account);
        assertEquals(Account.AccountType.CREDIT, account.getAccountType());
        assertTrue(account.getAccountNumber().startsWith("CRD-"));
        assertEquals(BigDecimal.valueOf(0.15), account.getInterestRate());
        assertEquals(new BigDecimal("5000.00"), account.getCreditLimit());
        assertEquals(BigDecimal.ZERO, account.getOverdraftLimit());
    }
    
    @Test
    void testCreateCustomAccount() {
        // Arrange
        BigDecimal customInterest = new BigDecimal("0.05");
        BigDecimal customCredit = new BigDecimal("10000.00");
        BigDecimal customOverdraft = new BigDecimal("1000.00");
        
        // Act
        Account account = accountFactory.createCustomAccount(
            Account.AccountType.CHECKING,
            testUser,
            customInterest,
            customCredit,
            customOverdraft
        );
        
        // Assert
        assertNotNull(account);
        assertEquals(customInterest, account.getInterestRate());
        assertEquals(customCredit, account.getCreditLimit());
        assertEquals(customOverdraft, account.getOverdraftLimit());
    }
    
    @Test
    void testAccountNumberUniqueness() {
        // Act
        Account account1 = accountFactory.createAccount(Account.AccountType.CHECKING, testUser);
        Account account2 = accountFactory.createAccount(Account.AccountType.CHECKING, testUser);
        
        // Assert
        assertNotEquals(account1.getAccountNumber(), account2.getAccountNumber());
    }
}
