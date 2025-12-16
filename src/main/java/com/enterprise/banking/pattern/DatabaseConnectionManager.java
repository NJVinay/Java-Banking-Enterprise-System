package com.enterprise.banking.pattern;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Singleton pattern implementation for database connection management.
 * Thread-safe lazy initialization with double-checked locking.
 */
@Slf4j
@Component
public class DatabaseConnectionManager {

    private static volatile DatabaseConnectionManager instance;
    private static final Lock lock = new ReentrantLock();

    private Connection connection;
    private String url;
    private String username;
    private String password;

    // Private constructor prevents instantiation
    private DatabaseConnectionManager() {
        log.info("Initializing DatabaseConnectionManager Singleton");
    }

    /**
     * Thread-safe singleton instance getter with double-checked locking.
     */
    public static DatabaseConnectionManager getInstance() {
        if (instance == null) {
            lock.lock();
            try {
                if (instance == null) {
                    instance = new DatabaseConnectionManager();
                }
            } finally {
                lock.unlock();
            }
        }
        return instance;
    }

    /**
     * Configure database connection parameters.
     */
    public void configure(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        log.info("Database connection configured for URL: {}", url);
    }

    /**
     * Get database connection with lazy initialization.
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            lock.lock();
            try {
                if (connection == null || connection.isClosed()) {
                    connection = DriverManager.getConnection(url, username, password);
                    log.info("New database connection established");
                }
            } finally {
                lock.unlock();
            }
        }
        return connection;
    }

    /**
     * Close database connection.
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                log.info("Database connection closed");
            } catch (SQLException e) {
                log.error("Error closing database connection", e);
            }
        }
    }

    /**
     * Test database connectivity.
     */
    public boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            log.error("Database connection test failed", e);
            return false;
        }
    }
}
