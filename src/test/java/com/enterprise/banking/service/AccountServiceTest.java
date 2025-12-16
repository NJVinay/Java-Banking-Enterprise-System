package com.enterprise.banking.service;

import com.enterprise.banking.model.Account;
import com.enterprise.banking.model.User;
import com.enterprise.banking.pattern.AccountFactory;
import com.enterprise.banking.repository.AccountRepository;
import com.enterprise.banking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * JUnit tests for AccountService.
 * Tests account creation using Factory pattern.
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountFactory accountFactory;

    @InjectMocks
    private AccountService accountService;

    private User testUser;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");

        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setAccountNumber("CHK-123456-ABCD");
        testAccount.setAccountType(Account.AccountType.CHECKING);
        testAccount.setBalance(new BigDecimal("1000.00"));
        testAccount.setUser(testUser);
        testAccount.setStatus(Account.AccountStatus.ACTIVE);
    }

    @Test
    void testCreateAccount_Success() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(accountFactory.createAccount(any(), any())).thenReturn(testAccount);
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // Act
        Account createdAccount = accountService.createAccount("testuser", Account.AccountType.CHECKING);

        // Assert
        assertNotNull(createdAccount);
        assertEquals("CHK-123456-ABCD", createdAccount.getAccountNumber());
        assertEquals(Account.AccountType.CHECKING, createdAccount.getAccountType());
        verify(accountFactory, times(1)).createAccount(any(), any());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void testCreateAccount_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            accountService.createAccount("nonexistent", Account.AccountType.CHECKING);
        });
    }

    @Test
    void testGetUserAccounts_Success() {
        // Arrange
        Account account2 = new Account();
        account2.setId(2L);
        account2.setAccountNumber("SAV-789012-EFGH");
        account2.setAccountType(Account.AccountType.SAVINGS);

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(accountRepository.findByUser(any(User.class)))
                .thenReturn(Arrays.asList(testAccount, account2));

        // Act
        List<Account> accounts = accountService.getUserAccounts("testuser");

        // Assert
        assertNotNull(accounts);
        assertEquals(2, accounts.size());
        verify(accountRepository, times(1)).findByUser(testUser);
    }

    @Test
    void testCloseAccount_Success() {
        // Arrange
        testAccount.setBalance(BigDecimal.ZERO);
        when(accountRepository.findByAccountNumber(anyString())).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // Act
        accountService.closeAccount("CHK-123456-ABCD");

        // Assert
        assertEquals(Account.AccountStatus.CLOSED, testAccount.getStatus());
        verify(accountRepository, times(1)).save(testAccount);
    }

    @Test
    void testCloseAccount_NonZeroBalance_ThrowsException() {
        // Arrange
        testAccount.setBalance(new BigDecimal("100.00"));
        when(accountRepository.findByAccountNumber(anyString())).thenReturn(Optional.of(testAccount));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            accountService.closeAccount("CHK-123456-ABCD");
        });
    }

    @Test
    void testGetTotalBalance_UsingStreamsAPI() {
        // Arrange
        Account account2 = new Account();
        account2.setBalance(new BigDecimal("500.00"));
        account2.setStatus(Account.AccountStatus.ACTIVE);

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(accountRepository.findByUser(any(User.class)))
                .thenReturn(Arrays.asList(testAccount, account2));

        // Act
        BigDecimal totalBalance = accountService.getTotalBalance("testuser");

        // Assert
        assertEquals(new BigDecimal("1500.00"), totalBalance);
    }

    @Test
    void testGetActiveAccountsCount_UsingStreamsAPI() {
        // Arrange
        Account inactiveAccount = new Account();
        inactiveAccount.setStatus(Account.AccountStatus.CLOSED);

        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(accountRepository.findByUser(any(User.class)))
                .thenReturn(Arrays.asList(testAccount, inactiveAccount));

        // Act
        long activeCount = accountService.getActiveAccountsCount("testuser");

        // Assert
        assertEquals(1, activeCount);
    }
}
