package com.enterprise.banking.service;

import com.enterprise.banking.model.Account;
import com.enterprise.banking.model.User;
import com.enterprise.banking.pattern.AccountFactory;
import com.enterprise.banking.repository.AccountRepository;
import com.enterprise.banking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service for account management operations.
 * Uses Factory pattern for account creation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountFactory accountFactory;

    /**
     * Create a new account using Factory pattern.
     */
    @Transactional
    public Account createAccount(String username, Account.AccountType accountType) {
        log.info("Creating {} account for user: {}", accountType, username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        // Use Factory pattern to create account
        Account account = accountFactory.createAccount(accountType, user);

        Account savedAccount = accountRepository.save(account);
        log.info("Account created successfully: {} for user: {}",
                savedAccount.getAccountNumber(), username);

        return savedAccount;
    }

    /**
     * Create custom account with specific parameters.
     */
    @Transactional
    public Account createCustomAccount(String username, Account.AccountType accountType,
            BigDecimal interestRate, BigDecimal creditLimit,
            BigDecimal overdraftLimit) {
        log.info("Creating custom {} account for user: {}", accountType, username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        Account account = accountFactory.createCustomAccount(
                accountType, user, interestRate, creditLimit, overdraftLimit);

        return accountRepository.save(account);
    }

    /**
     * Get all accounts for a user using Java 8 Streams.
     */
    @Transactional(readOnly = true)
    public List<Account> getUserAccounts(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        return accountRepository.findByUser(user);
    }

    /**
     * Get account by account number using Java 8 Optional.
     */
    @Transactional(readOnly = true)
    public Optional<Account> getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    /**
     * Close an account.
     */
    @Transactional
    public void closeAccount(String accountNumber) {
        log.info("Closing account: {}", accountNumber);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountNumber));

        // Check if balance is zero
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException(
                    "Cannot close account with non-zero balance: " + account.getBalance());
        }

        account.setStatus(Account.AccountStatus.CLOSED);
        accountRepository.save(account);

        log.info("Account closed successfully: {}", accountNumber);
    }

    /**
     * Suspend an account.
     */
    @Transactional
    public void suspendAccount(String accountNumber, String reason) {
        log.info("Suspending account: {} - Reason: {}", accountNumber, reason);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountNumber));

        account.setStatus(Account.AccountStatus.SUSPENDED);
        accountRepository.save(account);

        log.info("Account suspended successfully: {}", accountNumber);
    }

    /**
     * Activate a suspended account.
     */
    @Transactional
    public void activateAccount(String accountNumber) {
        log.info("Activating account: {}", accountNumber);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountNumber));

        account.setStatus(Account.AccountStatus.ACTIVE);
        accountRepository.save(account);

        log.info("Account activated successfully: {}", accountNumber);
    }

    /**
     * Get total balance across all accounts for a user using Java 8 Streams.
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalBalance(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        return accountRepository.findByUser(user).stream()
                .filter(Account::isActive)
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Get active accounts count using Java 8 Streams.
     */
    @Transactional(readOnly = true)
    public long getActiveAccountsCount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        return accountRepository.findByUser(user).stream()
                .filter(Account::isActive)
                .count();
    }
}
