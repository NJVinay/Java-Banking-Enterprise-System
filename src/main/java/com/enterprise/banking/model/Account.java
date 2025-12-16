package com.enterprise.banking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Account entity representing different types of bank accounts.
 * Supports CHECKING, SAVINGS, and CREDIT account types with ACID-compliant
 * transactions.
 */
@Entity
@Table(name = "accounts", indexes = {
        @Index(name = "idx_account_number", columnList = "accountNumber"),
        @Index(name = "idx_user_id", columnList = "user_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal interestRate = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    private BigDecimal creditLimit;

    @Column(precision = 15, scale = 2)
    private BigDecimal overdraftLimit = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status = AccountStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime lastTransactionAt;

    @Version
    private Long version; // For optimistic locking

    public enum AccountType {
        CHECKING(0.01, BigDecimal.ZERO),
        SAVINGS(0.02, BigDecimal.ZERO),
        CREDIT(0.15, new BigDecimal("5000.00"));

        private final double defaultInterestRate;
        private final BigDecimal defaultCreditLimit;

        AccountType(double defaultInterestRate, BigDecimal defaultCreditLimit) {
            this.defaultInterestRate = defaultInterestRate;
            this.defaultCreditLimit = defaultCreditLimit;
        }

        public double getDefaultInterestRate() {
            return defaultInterestRate;
        }

        public BigDecimal getDefaultCreditLimit() {
            return defaultCreditLimit;
        }
    }

    public enum AccountStatus {
        ACTIVE, INACTIVE, SUSPENDED, CLOSED
    }

    // Business logic methods
    public void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        this.balance = this.balance.add(amount);
        this.lastTransactionAt = LocalDateTime.now();
    }

    public void withdraw(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }

        BigDecimal availableBalance = this.balance.add(this.overdraftLimit);
        if (accountType == AccountType.CREDIT) {
            availableBalance = this.creditLimit.subtract(this.balance);
        }

        if (amount.compareTo(availableBalance) > 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        this.balance = this.balance.subtract(amount);
        this.lastTransactionAt = LocalDateTime.now();
    }

    public BigDecimal getAvailableBalance() {
        if (accountType == AccountType.CREDIT) {
            return creditLimit.subtract(balance);
        }
        return balance.add(overdraftLimit);
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }
}
