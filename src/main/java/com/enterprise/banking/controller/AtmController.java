package com.enterprise.banking.controller;

import com.enterprise.banking.dto.TransactionRequest;
import com.enterprise.banking.dto.TransactionResponse;
import com.enterprise.banking.service.BankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for ATM operations (MVC Pattern - Controller layer).
 * Simulates ATM machine interactions with the banking system.
 */
@Slf4j
@RestController
@RequestMapping("/api/atm")
@RequiredArgsConstructor
public class AtmController {

    private final BankingService bankingService;

    /**
     * ATM withdrawal operation.
     * POST /api/atm/withdraw
     */
    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> atmWithdraw(@RequestBody TransactionRequest request) {
        log.info("ATM - Withdrawal request for account: {}", request.getAccountNumber());

        // Set channel to ATM
        request.setChannel("ATM");

        try {
            TransactionResponse response = bankingService.withdraw(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("ATM withdrawal failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    TransactionResponse.builder()
                            .message("ATM withdrawal failed: " + e.getMessage())
                            .build());
        }
    }

    /**
     * ATM balance inquiry.
     * GET /api/atm/balance/{accountNumber}
     */
    @GetMapping("/balance/{accountNumber}")
    public ResponseEntity<TransactionResponse> atmBalanceInquiry(@PathVariable String accountNumber) {
        log.info("ATM - Balance inquiry for account: {}", accountNumber);

        try {
            TransactionResponse response = bankingService.getBalance(accountNumber);
            response.setChannel("ATM");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("ATM balance inquiry failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * ATM deposit operation.
     * POST /api/atm/deposit
     */
    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> atmDeposit(@RequestBody TransactionRequest request) {
        log.info("ATM - Deposit request for account: {}", request.getAccountNumber());

        // Set channel to ATM
        request.setChannel("ATM");

        try {
            TransactionResponse response = bankingService.deposit(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("ATM deposit failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    TransactionResponse.builder()
                            .message("ATM deposit failed: " + e.getMessage())
                            .build());
        }
    }
}
