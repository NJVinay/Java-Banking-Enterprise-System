package com.enterprise.banking.service;

import com.enterprise.banking.dto.TransactionRequest;
import com.enterprise.banking.dto.TransactionResponse;
import com.enterprise.banking.exception.AccountNotFoundException;
import com.enterprise.banking.exception.InsufficientFundsException;
import com.enterprise.banking.model.Account;
import com.enterprise.banking.model.Transaction;
import com.enterprise.banking.model.User;
import com.enterprise.banking.pattern.TransactionNotifier;
import com.enterprise.banking.repository.AccountRepository;
import com.enterprise.banking.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * JUnit tests for BankingService with Mockito.
 * Tests deposits, withdrawals, transfers, and balance inquiries.
 */
@ExtendWith(MockitoExtension.class)
class BankingServiceTest {
    
    @Mock
    private AccountRepository accountRepository;
    
    @Mock
    private TransactionRepository transactionRepository;
    
    @Mock
    private TransactionNotifier transactionNotifier;
    
    @InjectMocks
    private BankingService bankingService;
    
    private Account testAccount;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        // Set up test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
        
        // Set up test account
        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setAccountNumber("CHK-123456-ABCD");
        testAccount.setAccountType(Account.AccountType.CHECKING);
        testAccount.setBalance(new BigDecimal("1000.00"));
        testAccount.setUser(testUser);
        testAccount.setStatus(Account.AccountStatus.ACTIVE);
        testAccount.setOverdraftLimit(new BigDecimal("500.00"));
    }
    
    @Test
    void testDeposit_Success() {
        // Arrange
        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("CHK-123456-ABCD");
        request.setAmount(new BigDecimal("500.00"));
        request.setDescription("Test deposit");
        request.setChannel("ONLINE");
        
        when(accountRepository.findByAccountNumberForUpdate(anyString()))
            .thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(transactionRepository.save(any(Transaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        TransactionResponse response = bankingService.deposit(request);
        
        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("1500.00"), response.getBalanceAfter());
        assertEquals(Transaction.TransactionStatus.COMPLETED, response.getStatus());
        verify(accountRepository, times(1)).save(testAccount);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(transactionNotifier, times(1)).notifyTransactionInitiated(any());
        verify(transactionNotifier, times(1)).notifyTransactionCompleted(any());
    }
    
    @Test
    void testDeposit_NegativeAmount_ThrowsException() {
        // Arrange
        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("CHK-123456-ABCD");
        request.setAmount(new BigDecimal("-100.00"));
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            bankingService.deposit(request);
        });
    }
    
    @Test
    void testDeposit_AccountNotFound_ThrowsException() {
        // Arrange
        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("INVALID-ACCOUNT");
        request.setAmount(new BigDecimal("100.00"));
        
        when(accountRepository.findByAccountNumberForUpdate(anyString()))
            .thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(AccountNotFoundException.class, () -> {
            bankingService.deposit(request);
        });
    }
    
    @Test
    void testWithdraw_Success() {
        // Arrange
        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("CHK-123456-ABCD");
        request.setAmount(new BigDecimal("200.00"));
        request.setDescription("Test withdrawal");
        request.setChannel("ATM");
        
        when(accountRepository.findByAccountNumberForUpdate(anyString()))
            .thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);
        when(transactionRepository.save(any(Transaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        TransactionResponse response = bankingService.withdraw(request);
        
        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("800.00"), response.getBalanceAfter());
        assertEquals(Transaction.TransactionStatus.COMPLETED, response.getStatus());
        verify(accountRepository, times(1)).save(testAccount);
    }
    
    @Test
    void testWithdraw_InsufficientFunds_ThrowsException() {
        // Arrange
        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("CHK-123456-ABCD");
        request.setAmount(new BigDecimal("2000.00")); // More than balance + overdraft
        
        when(accountRepository.findByAccountNumberForUpdate(anyString()))
            .thenReturn(Optional.of(testAccount));
        when(transactionRepository.save(any(Transaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act & Assert
        assertThrows(InsufficientFundsException.class, () -> {
            bankingService.withdraw(request);
        });
        verify(transactionNotifier, times(1)).notifyTransactionFailed(any(), anyString());
    }
    
    @Test
    void testTransfer_Success() {
        // Arrange
        Account targetAccount = new Account();
        targetAccount.setId(2L);
        targetAccount.setAccountNumber("SAV-789012-EFGH");
        targetAccount.setAccountType(Account.AccountType.SAVINGS);
        targetAccount.setBalance(new BigDecimal("500.00"));
        targetAccount.setUser(testUser);
        targetAccount.setStatus(Account.AccountStatus.ACTIVE);
        
        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("CHK-123456-ABCD");
        request.setTargetAccountNumber("SAV-789012-EFGH");
        request.setAmount(new BigDecimal("300.00"));
        request.setChannel("ONLINE");
        
        when(accountRepository.findByAccountNumberForUpdate("CHK-123456-ABCD"))
            .thenReturn(Optional.of(testAccount));
        when(accountRepository.findByAccountNumberForUpdate("SAV-789012-EFGH"))
            .thenReturn(Optional.of(targetAccount));
        when(accountRepository.save(any(Account.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        TransactionResponse response = bankingService.transfer(request);
        
        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("700.00"), response.getBalanceAfter());
        assertEquals(new BigDecimal("800.00"), targetAccount.getBalance());
        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }
    
    @Test
    void testTransfer_SameAccount_ThrowsException() {
        // Arrange
        TransactionRequest request = new TransactionRequest();
        request.setAccountNumber("CHK-123456-ABCD");
        request.setTargetAccountNumber("CHK-123456-ABCD");
        request.setAmount(new BigDecimal("100.00"));
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            bankingService.transfer(request);
        });
    }
    
    @Test
    void testGetBalance_Success() {
        // Arrange
        when(accountRepository.findByAccountNumber(anyString()))
            .thenReturn(Optional.of(testAccount));
        when(transactionRepository.save(any(Transaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        
        // Act
        TransactionResponse response = bankingService.getBalance("CHK-123456-ABCD");
        
        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("1000.00"), response.getBalanceAfter());
        assertEquals(Transaction.TransactionType.BALANCE_INQUIRY, response.getTransactionType());
    }
    
    @Test
    void testGetTotalDeposits_UsingStreamsAPI() {
        // Arrange
        when(accountRepository.findByAccountNumber(anyString()))
            .thenReturn(Optional.of(testAccount));
        when(transactionRepository.findByAccount(any(Account.class)))
            .thenReturn(java.util.Arrays.asList(
                createTransaction(Transaction.TransactionType.DEPOSIT, new BigDecimal("100.00")),
                createTransaction(Transaction.TransactionType.DEPOSIT, new BigDecimal("200.00")),
                createTransaction(Transaction.TransactionType.WITHDRAWAL, new BigDecimal("50.00"))
            ));
        
        // Act
        BigDecimal totalDeposits = bankingService.getTotalDeposits("CHK-123456-ABCD");
        
        // Assert
        assertEquals(new BigDecimal("300.00"), totalDeposits);
    }
    
    private Transaction createTransaction(Transaction.TransactionType type, BigDecimal amount) {
        Transaction txn = new Transaction();
        txn.setTransactionType(type);
        txn.setAmount(amount);
        txn.setStatus(Transaction.TransactionStatus.COMPLETED);
        txn.setAccount(testAccount);
        return txn;
    }
}
