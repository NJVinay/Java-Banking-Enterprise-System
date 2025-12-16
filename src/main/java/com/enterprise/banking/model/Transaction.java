package com.enterprise.banking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction entity representing all banking transactions with ACID
 * compliance.
 * Supports deposits, withdrawals, transfers, and balance inquiries.
 */
@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_transaction_date", columnList = "transactionDate"),
        @Index(name = "idx_account_id", columnList = "account_id"),
        @Index(name = "idx_transaction_type", columnList = "transactionType"),
        @Index(name = "idx_reference_number", columnList = "referenceNumber")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String referenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(length = 20)
    private String targetAccountNumber; // For transfers

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime transactionDate;

    private LocalDateTime processedDate;

    @Column(length = 100)
    private String processedBy; // Username of teller/system

    @Column(length = 500)
    private String failureReason;

    private String ipAddress;

    @Column(length = 50)
    private String channel = "ONLINE"; // ONLINE, ATM, BRANCH, MOBILE

    public enum TransactionType {
        DEPOSIT,
        WITHDRAWAL,
        TRANSFER_OUT,
        TRANSFER_IN,
        BALANCE_INQUIRY,
        INTEREST_CREDIT,
        FEE_DEBIT,
        LOAN_PAYMENT,
        BILL_PAYMENT
    }

    public enum TransactionStatus {
        PENDING,
        COMPLETED,
        FAILED,
        REVERSED,
        CANCELLED
    }

    // Helper methods
    public void markAsCompleted() {
        this.status = TransactionStatus.COMPLETED;
        this.processedDate = LocalDateTime.now();
    }

    public void markAsFailed(String reason) {
        this.status = TransactionStatus.FAILED;
        this.failureReason = reason;
        this.processedDate = LocalDateTime.now();
    }

    public boolean isSuccessful() {
        return status == TransactionStatus.COMPLETED;
    }

    public boolean isMoneyTransaction() {
        return transactionType == TransactionType.DEPOSIT ||
                transactionType == TransactionType.WITHDRAWAL ||
                transactionType == TransactionType.TRANSFER_OUT ||
                transactionType == TransactionType.TRANSFER_IN;
    }
}
