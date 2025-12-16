package com.enterprise.banking.controller;

import com.enterprise.banking.dto.TransactionRequest;
import com.enterprise.banking.dto.TransactionResponse;
import com.enterprise.banking.service.BankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for banking operations (MVC Pattern - Controller layer).
 * Provides RESTful APIs for deposits, withdrawals, transfers, and balance
 * inquiries.
 */
@Slf4j
@RestController
@RequestMapping("/api/banking")
@RequiredArgsConstructor
public class BankingController {

    private final BankingService bankingService;

    /**
     * Deposit money into an account.
     * POST /api/banking/deposit
     */
    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(@RequestBody TransactionRequest request) {
        log.info("REST API - Deposit request received for account: {}", request.getAccountNumber());

        try {
            TransactionResponse response = bankingService.deposit(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Deposit failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    TransactionResponse.builder()
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Deposit error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    TransactionResponse.builder()
                            .message("An error occurred processing the deposit")
                            .build());
        }
    }

    /**
     * Withdraw money from an account.
     * POST /api/banking/withdraw
     */
    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(@RequestBody TransactionRequest request) {
        log.info("REST API - Withdrawal request received for account: {}", request.getAccountNumber());

        try {
            TransactionResponse response = bankingService.withdraw(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Withdrawal failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    TransactionResponse.builder()
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Withdrawal error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    TransactionResponse.builder()
                            .message("An error occurred processing the withdrawal")
                            .build());
        }
    }

    /**
     * Transfer money between accounts.
     * POST /api/banking/transfer
     */
    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@RequestBody TransactionRequest request) {
        log.info("REST API - Transfer request received from {} to {}",
                request.getAccountNumber(), request.getTargetAccountNumber());

        try {
            TransactionResponse response = bankingService.transfer(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Transfer failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    TransactionResponse.builder()
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            log.error("Transfer error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    TransactionResponse.builder()
                            .message("An error occurred processing the transfer")
                            .build());
        }
    }

    /**
     * Get account balance.
     * GET /api/banking/balance/{accountNumber}
     */
    @GetMapping("/balance/{accountNumber}")
    public ResponseEntity<TransactionResponse> getBalance(@PathVariable String accountNumber) {
        log.info("REST API - Balance inquiry for account: {}", accountNumber);

        try {
            TransactionResponse response = bankingService.getBalance(accountNumber);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Balance inquiry failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Balance inquiry error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get transaction history for an account.
     * GET /api/banking/transactions/{accountNumber}
     */
    @GetMapping("/transactions/{accountNumber}")
    public ResponseEntity<List<TransactionResponse>> getTransactionHistory(
            @PathVariable String accountNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.info("REST API - Transaction history request for account: {}", accountNumber);

        try {
            List<TransactionResponse> transactions = bankingService.getTransactionHistory(accountNumber, startDate,
                    endDate);
            return ResponseEntity.ok(transactions);
        } catch (IllegalArgumentException e) {
            log.error("Transaction history failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Transaction history error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get total deposits for an account.
     * GET /api/banking/deposits/{accountNumber}/total
     */
    @GetMapping("/deposits/{accountNumber}/total")
    public ResponseEntity<BigDecimal> getTotalDeposits(@PathVariable String accountNumber) {
        log.info("REST API - Total deposits request for account: {}", accountNumber);

        try {
            BigDecimal total = bankingService.getTotalDeposits(accountNumber);
            return ResponseEntity.ok(total);
        } catch (IllegalArgumentException e) {
            log.error("Total deposits failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Total deposits error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
