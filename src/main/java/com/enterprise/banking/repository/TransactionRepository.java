package com.enterprise.banking.repository;

import com.enterprise.banking.model.Account;
import com.enterprise.banking.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * DAO pattern implementation for Transaction entity.
 * Supports complex queries for transaction history and reporting.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find transaction by reference number.
     */
    Optional<Transaction> findByReferenceNumber(String referenceNumber);

    /**
     * Find all transactions for an account.
     */
    List<Transaction> findByAccount(Account account);

    /**
     * Find transactions for an account ordered by date descending.
     */
    List<Transaction> findByAccountOrderByTransactionDateDesc(Account account);

    /**
     * Find transactions by type.
     */
    List<Transaction> findByTransactionType(Transaction.TransactionType transactionType);

    /**
     * Find transactions by status.
     */
    List<Transaction> findByStatus(Transaction.TransactionStatus status);

    /**
     * Find transactions for an account within date range.
     */
    @Query("SELECT t FROM Transaction t WHERE t.account = :account " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate " +
            "ORDER BY t.transactionDate DESC")
    List<Transaction> findByAccountAndDateRange(
            @Param("account") Account account,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find transactions above specified amount.
     */
    @Query("SELECT t FROM Transaction t WHERE t.amount > :amount")
    List<Transaction> findLargeTransactions(@Param("amount") BigDecimal amount);

    /**
     * Find failed transactions for an account.
     */
    @Query("SELECT t FROM Transaction t WHERE t.account = :account " +
            "AND t.status = 'FAILED' ORDER BY t.transactionDate DESC")
    List<Transaction> findFailedTransactionsByAccount(@Param("account") Account account);

    /**
     * Get transaction statistics for an account.
     */
    @Query("SELECT t.transactionType, COUNT(t), SUM(t.amount) FROM Transaction t " +
            "WHERE t.account = :account AND t.status = 'COMPLETED' " +
            "GROUP BY t.transactionType")
    List<Object[]> getTransactionStatisticsByAccount(@Param("account") Account account);

    /**
     * Find recent transactions for an account (last N transactions).
     */
    @Query("SELECT t FROM Transaction t WHERE t.account = :account " +
            "ORDER BY t.transactionDate DESC")
    List<Transaction> findRecentTransactions(@Param("account") Account account);

    /**
     * Get total transaction amount by type for an account.
     */
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.account = :account " +
            "AND t.transactionType = :type AND t.status = 'COMPLETED'")
    BigDecimal getTotalAmountByTypeAndAccount(
            @Param("account") Account account,
            @Param("type") Transaction.TransactionType type);

    /**
     * Count pending transactions.
     */
    long countByStatus(Transaction.TransactionStatus status);

    /**
     * Find transactions by channel (ATM, ONLINE, etc.).
     */
    List<Transaction> findByChannel(String channel);
}
