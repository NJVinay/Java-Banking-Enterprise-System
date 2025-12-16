package com.enterprise.banking.dto;

import com.enterprise.banking.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for transaction responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {
    private String referenceNumber;
    private Transaction.TransactionType transactionType;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String accountNumber;
    private String targetAccountNumber;
    private String description;
    private Transaction.TransactionStatus status;
    private LocalDateTime transactionDate;
    private String channel;
    private String message;
}
