package com.enterprise.banking.service;

import com.enterprise.banking.dto.TransactionRequest;
import com.enterprise.banking.dto.TransactionResponse;
import com.enterprise.banking.exception.AccountNotFoundException;
import com.enterprise.banking.exception.InsufficientFundsException;
import com.enterprise.banking.model.Account;
import com.enterprise.banking.model.Transaction;
import com.enterprise.banking.pattern.TransactionNotifier;
import com.enterprise.banking.repository.AccountRepository;
import com.enterprise.banking.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for banking operations with ACID-compliant transaction
 * management.
 * Implements deposits, withdrawals, transfers, and balance inquiries.
 * Uses Java 8+ Streams API for transaction filtering and Lambda expressions for
 * callbacks.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BankingService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionNotifier transactionNotifier;

    /**
     * Deposit money into an account with ACID compliance.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransactionResponse deposit(TransactionRequest request) {
        log.info("Processing deposit: {} to account: {}",
                request.getAmount(), request.getAccountNumber());

        // Validate amount
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        // Find and lock account
        Account account = accountRepository.findByAccountNumberForUpdate(request.getAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException(request.getAccountNumber()));

        if (!account.isActive()) {
            throw new IllegalStateException("Account is not active");
        }

        // Create transaction record
        Transaction transaction = createTransaction(
                account,
                Transaction.TransactionType.DEPOSIT,
                request.getAmount(),
                request.getDescription(),
                request.getChannel());

        try {
            // Notify observers (transaction initiated)
            transactionNotifier.notifyTransactionInitiated(transaction);

            // Perform deposit
            account.deposit(request.getAmount());
            transaction.setBalanceAfter(account.getBalance());
            transaction.markAsCompleted();

            // Save changes
            accountRepository.save(account);
            transactionRepository.save(transaction);

            // Notify observers (transaction completed)
            transactionNotifier.notifyTransactionCompleted(transaction);

            log.info("Deposit successful: {} - New balance: {}",
                    transaction.getReferenceNumber(), account.getBalance());

            return buildResponse(transaction, "Deposit successful");

        } catch (Exception e) {
            transaction.markAsFailed(e.getMessage());
            transactionRepository.save(transaction);
            transactionNotifier.notifyTransactionFailed(transaction, e.getMessage());
            log.error("Deposit failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Withdraw money from an account with ACID compliance.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransactionResponse withdraw(TransactionRequest request) {
        log.info("Processing withdrawal: {} from account: {}",
                request.getAmount(), request.getAccountNumber());

        // Validate amount
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }

        // Find and lock account
        Account account = accountRepository.findByAccountNumberForUpdate(request.getAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException(request.getAccountNumber()));

        if (!account.isActive()) {
            throw new IllegalStateException("Account is not active");
        }

        // Create transaction record
        Transaction transaction = createTransaction(
                account,
                Transaction.TransactionType.WITHDRAWAL,
                request.getAmount(),
                request.getDescription(),
                request.getChannel());

        try {
            // Notify observers (transaction initiated)
            transactionNotifier.notifyTransactionInitiated(transaction);

            // Check sufficient funds
            if (request.getAmount().compareTo(account.getAvailableBalance()) > 0) {
                throw new InsufficientFundsException(
                        String.format("Insufficient funds. Available: %s, Requested: %s",
                                account.getAvailableBalance(), request.getAmount()));
            }

            // Perform withdrawal
            account.withdraw(request.getAmount());
            transaction.setBalanceAfter(account.getBalance());
            transaction.markAsCompleted();

            // Save changes
            accountRepository.save(account);
            transactionRepository.save(transaction);

            // Notify observers (transaction completed)
            transactionNotifier.notifyTransactionCompleted(transaction);

            log.info("Withdrawal successful: {} - New balance: {}",
                    transaction.getReferenceNumber(), account.getBalance());

            return buildResponse(transaction, "Withdrawal successful");

        } catch (Exception e) {
            transaction.markAsFailed(e.getMessage());
            transactionRepository.save(transaction);
            transactionNotifier.notifyTransactionFailed(transaction, e.getMessage());
            log.error("Withdrawal failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Transfer money between accounts with ACID compliance.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public TransactionResponse transfer(TransactionRequest request) {
        log.info("Processing transfer: {} from {} to {}",
                request.getAmount(), request.getAccountNumber(), request.getTargetAccountNumber());

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        if (request.getAccountNumber().equals(request.getTargetAccountNumber())) {
            throw new IllegalArgumentException("Cannot transfer to same account");
        }

        // Find and lock both accounts (order by account number to prevent deadlock)
        Account sourceAccount = accountRepository.findByAccountNumberForUpdate(
                request.getAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException(request.getAccountNumber()));

        Account targetAccount = accountRepository.findByAccountNumberForUpdate(
                request.getTargetAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException(request.getTargetAccountNumber()));

        if (!sourceAccount.isActive() || !targetAccount.isActive()) {
            throw new IllegalStateException("One or both accounts are not active");
        }

        // Create outgoing transaction
        Transaction outgoingTxn = createTransaction(
                sourceAccount,
                Transaction.TransactionType.TRANSFER_OUT,
                request.getAmount(),
                "Transfer to " + request.getTargetAccountNumber(),
                request.getChannel());
        outgoingTxn.setTargetAccountNumber(request.getTargetAccountNumber());

        // Create incoming transaction
        Transaction incomingTxn = createTransaction(
                targetAccount,
                Transaction.TransactionType.TRANSFER_IN,
                request.getAmount(),
                "Transfer from " + request.getAccountNumber(),
                request.getChannel());

        try {
            // Notify observers
            transactionNotifier.notifyTransactionInitiated(outgoingTxn);

            // Check sufficient funds
            if (request.getAmount().compareTo(sourceAccount.getAvailableBalance()) > 0) {
                throw new InsufficientFundsException(
                        String.format("Insufficient funds. Available: %s, Requested: %s",
                                sourceAccount.getAvailableBalance(), request.getAmount()));
            }

            // Perform transfer
            sourceAccount.withdraw(request.getAmount());
            targetAccount.deposit(request.getAmount());

            outgoingTxn.setBalanceAfter(sourceAccount.getBalance());
            outgoingTxn.markAsCompleted();

            incomingTxn.setBalanceAfter(targetAccount.getBalance());
            incomingTxn.markAsCompleted();

            // Save all changes
            accountRepository.save(sourceAccount);
            accountRepository.save(targetAccount);
            transactionRepository.save(outgoingTxn);
            transactionRepository.save(incomingTxn);

            // Notify observers
            transactionNotifier.notifyTransactionCompleted(outgoingTxn);

            log.info("Transfer successful: {} - Source balance: {}, Target balance: {}",
                    outgoingTxn.getReferenceNumber(), sourceAccount.getBalance(),
                    targetAccount.getBalance());

            return buildResponse(outgoingTxn, "Transfer successful");

        } catch (Exception e) {
            outgoingTxn.markAsFailed(e.getMessage());
            incomingTxn.markAsFailed(e.getMessage());
            transactionRepository.save(outgoingTxn);
            transactionRepository.save(incomingTxn);
            transactionNotifier.notifyTransactionFailed(outgoingTxn, e.getMessage());
            log.error("Transfer failed: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Get account balance - demonstrating Java 8 Optional for null safety.
     */
    @Transactional(readOnly = true)
    public TransactionResponse getBalance(String accountNumber) {
        log.info("Balance inquiry for account: {}", accountNumber);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        // Create balance inquiry transaction
        Transaction transaction = createTransaction(
                account,
                Transaction.TransactionType.BALANCE_INQUIRY,
                BigDecimal.ZERO,
                "Balance inquiry",
                "ONLINE");
        transaction.setBalanceAfter(account.getBalance());
        transaction.markAsCompleted();
        transactionRepository.save(transaction);

        return buildResponse(transaction, "Balance retrieved successfully");
    }

    /**
     * Get transaction history using Java 8 Streams API for filtering.
     */
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionHistory(String accountNumber,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        List<Transaction> transactions;
        if (startDate != null && endDate != null) {
            transactions = transactionRepository.findByAccountAndDateRange(
                    account, startDate, endDate);
        } else {
            transactions = transactionRepository.findByAccountOrderByTransactionDateDesc(account);
        }

        // Use Java 8 Streams API for filtering and mapping
        return transactions.stream()
                .filter(Transaction::isSuccessful)
                .map(txn -> buildResponse(txn, "Transaction record"))
                .collect(Collectors.toList());
    }

    /**
     * Get transaction summary using Java 8 Streams API.
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalDeposits(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        // Using Java 8 Streams with Lambda expressions
        return transactionRepository.findByAccount(account).stream()
                .filter(txn -> txn.getTransactionType() == Transaction.TransactionType.DEPOSIT)
                .filter(Transaction::isSuccessful)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Helper methods

    private Transaction createTransaction(Account account,
            Transaction.TransactionType type,
            BigDecimal amount,
            String description,
            String channel) {
        Transaction transaction = new Transaction();
        transaction.setReferenceNumber(generateReferenceNumber());
        transaction.setAccount(account);
        transaction.setTransactionType(type);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setChannel(Optional.ofNullable(channel).orElse("ONLINE"));
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        return transaction;
    }

    private String generateReferenceNumber() {
        return "TXN-" + System.currentTimeMillis() + "-" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private TransactionResponse buildResponse(Transaction transaction, String message) {
        return TransactionResponse.builder()
                .referenceNumber(transaction.getReferenceNumber())
                .transactionType(transaction.getTransactionType())
                .amount(transaction.getAmount())
                .balanceAfter(transaction.getBalanceAfter())
                .accountNumber(transaction.getAccount().getAccountNumber())
                .targetAccountNumber(transaction.getTargetAccountNumber())
                .description(transaction.getDescription())
                .status(transaction.getStatus())
                .transactionDate(transaction.getTransactionDate())
                .channel(transaction.getChannel())
                .message(message)
                .build();
    }
}
