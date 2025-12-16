package com.enterprise.banking;

import com.enterprise.banking.pattern.EmailNotificationObserver;
import com.enterprise.banking.pattern.SmsNotificationObserver;
import com.enterprise.banking.pattern.TransactionNotifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main Spring Boot application class for Java Enterprise Banking System.
 * 
 * Features:
 * - Three-tier architecture (Presentation, Business Logic, Data layers)
 * - Spring Boot RESTful APIs
 * - MySQL database with JDBC and Hibernate ORM
 * - BCrypt password hashing
 * - ACID-compliant transaction management
 * - Design Patterns: Singleton, Factory, DAO, MVC, Observer
 * - Java 8+ features: Streams API, Lambda expressions, Optional, Collections
 */
@Slf4j
@SpringBootApplication
@EnableTransactionManagement
@EnableJpaAuditing
public class BankingSystemApplication {

    public static void main(String[] args) {
        log.info("========================================");
        log.info("Starting Java Enterprise Banking System");
        log.info("========================================");

        SpringApplication.run(BankingSystemApplication.class, args);

        log.info("========================================");
        log.info("Banking System Started Successfully");
        log.info("Access at: http://localhost:8080");
        log.info("========================================");
    }

    /**
     * Initialize Observer pattern by registering observers for transaction
     * notifications.
     */
    @Bean
    CommandLineRunner initObservers(TransactionNotifier notifier,
            EmailNotificationObserver emailObserver,
            SmsNotificationObserver smsObserver) {
        return args -> {
            log.info("Initializing Observer Pattern - Registering transaction observers");
            notifier.registerObserver(emailObserver);
            notifier.registerObserver(smsObserver);
            log.info("Observers registered successfully. Total observers: {}",
                    notifier.getObserverCount());
        };
    }
}
