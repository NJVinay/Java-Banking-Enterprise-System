package com.enterprise.banking.pattern;

import com.enterprise.banking.model.Account;
import com.enterprise.banking.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Factory pattern implementation for creating different types of bank accounts.
 * Encapsulates account creation logic and default configurations.
 */
@Slf4j
@Component
public class AccountFactory {

    /**
     * Create an account based on the specified type with default configurations.
     */
    public Account createAccount(Account.AccountType accountType, User user) {
        Account account = new Account();
        account.setAccountNumber(generateAccountNumber(accountType));
        account.setAccountType(accountType);
        account.setUser(user);
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(Account.AccountStatus.ACTIVE);

        // Configure account based on type
        switch (accountType) {
            case CHECKING:
                configureCheckingAccount(account);
                break;
            case SAVINGS:
                configureSavingsAccount(account);
                break;
            case CREDIT:
                configureCreditAccount(account);
                break;
            default:
                throw new IllegalArgumentException("Unknown account type: " + accountType);
        }

        log.info("Created {} account: {} for user: {}",
                accountType, account.getAccountNumber(), user.getUsername());
        return account;
    }

    /**
     * Configure checking account with specific parameters.
     */
    private void configureCheckingAccount(Account account) {
        account.setInterestRate(BigDecimal.valueOf(
                Account.AccountType.CHECKING.getDefaultInterestRate()));
        account.setOverdraftLimit(new BigDecimal("500.00")); // $500 overdraft
        account.setCreditLimit(null);
        log.debug("Configured checking account with 0.01% interest and $500 overdraft");
    }

    /**
     * Configure savings account with specific parameters.
     */
    private void configureSavingsAccount(Account account) {
        account.setInterestRate(BigDecimal.valueOf(
                Account.AccountType.SAVINGS.getDefaultInterestRate()));
        account.setOverdraftLimit(BigDecimal.ZERO); // No overdraft
        account.setCreditLimit(null);
        log.debug("Configured savings account with 0.02% interest and no overdraft");
    }

    /**
     * Configure credit account with specific parameters.
     */
    private void configureCreditAccount(Account account) {
        account.setInterestRate(BigDecimal.valueOf(
                Account.AccountType.CREDIT.getDefaultInterestRate()));
        account.setOverdraftLimit(BigDecimal.ZERO);
        account.setCreditLimit(Account.AccountType.CREDIT.getDefaultCreditLimit());
        log.debug("Configured credit account with 0.15% interest and $5000 credit limit");
    }

    /**
     * Generate unique account number based on account type.
     * Format: [Type Prefix]-[Timestamp]-[Random]
     */
    private String generateAccountNumber(Account.AccountType accountType) {
        String prefix;
        switch (accountType) {
            case CHECKING:
                prefix = "CHK";
                break;
            case SAVINGS:
                prefix = "SAV";
                break;
            case CREDIT:
                prefix = "CRD";
                break;
            default:
                prefix = "ACC";
        }

        String timestamp = String.valueOf(System.currentTimeMillis()).substring(5);
        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "%s-%s-%s".formatted(prefix, timestamp, random);
    }

    /**
     * Create a custom account with specified parameters.
     */
    public Account createCustomAccount(Account.AccountType accountType, User user,
            BigDecimal interestRate, BigDecimal creditLimit,
            BigDecimal overdraftLimit) {
        Account account = createAccount(accountType, user);

        if (interestRate != null) {
            account.setInterestRate(interestRate);
        }
        if (creditLimit != null) {
            account.setCreditLimit(creditLimit);
        }
        if (overdraftLimit != null) {
            account.setOverdraftLimit(overdraftLimit);
        }

        log.info("Created custom {} account with interest: {}, credit: {}, overdraft: {}",
                accountType, interestRate, creditLimit, overdraftLimit);
        return account;
    }
}
