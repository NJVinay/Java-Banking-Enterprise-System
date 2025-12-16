package com.enterprise.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object for banking operations requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {
    private String accountNumber;
    private BigDecimal amount;
    private String targetAccountNumber; // For transfers
    private String description;
    private String channel; // ONLINE, ATM, BRANCH, MOBILE
}
