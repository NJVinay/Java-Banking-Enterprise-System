package com.enterprise.banking.repository;

import com.enterprise.banking.model.Account;
import com.enterprise.banking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * DAO pattern implementation for Account entity.
 * Provides optimistic locking for ACID-compliant transactions.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Find account by account number.
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Find account by account number with pessimistic lock for transactions.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber")
    Optional<Account> findByAccountNumberForUpdate(@Param("accountNumber") String accountNumber);

    /**
     * Find all accounts for a user.
     */
    List<Account> findByUser(User user);

    /**
     * Find all accounts for a user ID.
     */
    List<Account> findByUserId(Long userId);

    /**
     * Find accounts by type.
     */
    List<Account> findByAccountType(Account.AccountType accountType);

    /**
     * Find accounts by status.
     */
    List<Account> findByStatus(Account.AccountStatus status);

    /**
     * Find active accounts for a user.
     */
    @Query("SELECT a FROM Account a WHERE a.user = :user AND a.status = 'ACTIVE'")
    List<Account> findActiveAccountsByUser(@Param("user") User user);

    /**
     * Find accounts with balance greater than specified amount.
     */
    @Query("SELECT a FROM Account a WHERE a.balance > :amount")
    List<Account> findAccountsWithBalanceGreaterThan(@Param("amount") BigDecimal amount);

    /**
     * Find dormant accounts (no transactions in specified days).
     */
    @Query("SELECT a FROM Account a WHERE a.lastTransactionAt < :cutoffDate OR a.lastTransactionAt IS NULL")
    List<Account> findDormantAccounts(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Get total balance across all accounts for a user.
     */
    @Query("SELECT SUM(a.balance) FROM Account a WHERE a.user = :user")
    BigDecimal getTotalBalanceByUser(@Param("user") User user);

    /**
     * Count accounts by type.
     */
    long countByAccountType(Account.AccountType accountType);

    /**
     * Check if account number exists.
     */
    boolean existsByAccountNumber(String accountNumber);
}
