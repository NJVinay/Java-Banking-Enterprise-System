package com.enterprise.banking.controller;

import com.enterprise.banking.model.Account;
import com.enterprise.banking.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST Controller for account management (MVC Pattern - Controller layer).
 */
@Slf4j
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * Create a new account.
     * POST /api/accounts
     */
    @PostMapping
    public ResponseEntity<Account> createAccount(
            @RequestParam String username,
            @RequestParam Account.AccountType accountType) {

        log.info("REST API - Create account request for user: {}, type: {}", username, accountType);

        try {
            Account account = accountService.createAccount(username, accountType);
            return ResponseEntity.status(HttpStatus.CREATED).body(account);
        } catch (IllegalArgumentException e) {
            log.error("Account creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Account creation error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all accounts for a user.
     * GET /api/accounts/user/{username}
     */
    @GetMapping("/user/{username}")
    public ResponseEntity<List<Account>> getUserAccounts(@PathVariable String username) {
        log.info("REST API - Get accounts for user: {}", username);

        try {
            List<Account> accounts = accountService.getUserAccounts(username);
            return ResponseEntity.ok(accounts);
        } catch (IllegalArgumentException e) {
            log.error("Get user accounts failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Get user accounts error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get account by account number.
     * GET /api/accounts/{accountNumber}
     */
    @GetMapping("/{accountNumber}")
    public ResponseEntity<Account> getAccount(@PathVariable String accountNumber) {
        log.info("REST API - Get account: {}", accountNumber);

        return accountService.getAccountByNumber(accountNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Close an account.
     * DELETE /api/accounts/{accountNumber}
     */
    @DeleteMapping("/{accountNumber}")
    public ResponseEntity<Void> closeAccount(@PathVariable String accountNumber) {
        log.info("REST API - Close account: {}", accountNumber);

        try {
            accountService.closeAccount(accountNumber);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Close account failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Close account error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Suspend an account.
     * PUT /api/accounts/{accountNumber}/suspend
     */
    @PutMapping("/{accountNumber}/suspend")
    public ResponseEntity<Void> suspendAccount(
            @PathVariable String accountNumber,
            @RequestParam String reason) {

        log.info("REST API - Suspend account: {}", accountNumber);

        try {
            accountService.suspendAccount(accountNumber, reason);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Suspend account failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Suspend account error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Activate an account.
     * PUT /api/accounts/{accountNumber}/activate
     */
    @PutMapping("/{accountNumber}/activate")
    public ResponseEntity<Void> activateAccount(@PathVariable String accountNumber) {
        log.info("REST API - Activate account: {}", accountNumber);

        try {
            accountService.activateAccount(accountNumber);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Activate account failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Activate account error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get total balance for a user.
     * GET /api/accounts/user/{username}/total-balance
     */
    @GetMapping("/user/{username}/total-balance")
    public ResponseEntity<BigDecimal> getTotalBalance(@PathVariable String username) {
        log.info("REST API - Get total balance for user: {}", username);

        try {
            BigDecimal totalBalance = accountService.getTotalBalance(username);
            return ResponseEntity.ok(totalBalance);
        } catch (IllegalArgumentException e) {
            log.error("Get total balance failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Get total balance error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
