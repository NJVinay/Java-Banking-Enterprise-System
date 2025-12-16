# SQL Scripts for Database Initialization

-- Create database
CREATE DATABASE IF NOT EXISTS banking_system
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE banking_system;

-- Users table will be auto-created by Hibernate
-- But here are some sample data inserts for testing

-- Sample users (password is 'TestPass123!' hashed with BCrypt)
-- INSERT INTO users (username, password, email, full_name, role, active, locked, failed_login_attempts, created_at, updated_at)
-- VALUES 
-- ('admin', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5Y/YBCHaJIGii', 'admin@banking.com', 'System Administrator', 'ADMIN', 1, 0, 0, NOW(), NOW()),
-- ('customer1', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5Y/YBCHaJIGii', 'customer1@example.com', 'John Doe', 'CUSTOMER', 1, 0, 0, NOW(), NOW()),
-- ('teller1', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5Y/YBCHaJIGii', 'teller1@banking.com', 'Jane Smith', 'TELLER', 1, 0, 0, NOW(), NOW());

-- Useful queries for administration

-- Check active users
-- SELECT username, email, role, active, locked FROM users WHERE active = 1;

-- Get account balances
-- SELECT a.account_number, a.account_type, a.balance, a.status, u.username
-- FROM accounts a
-- JOIN users u ON a.user_id = u.id
-- WHERE a.status = 'ACTIVE';

-- Get transaction history
-- SELECT t.reference_number, t.transaction_type, t.amount, t.balance_after, 
--        t.transaction_date, t.status, a.account_number
-- FROM transactions t
-- JOIN accounts a ON t.account_id = a.id
-- WHERE t.transaction_date >= DATE_SUB(NOW(), INTERVAL 7 DAY)
-- ORDER BY t.transaction_date DESC;

-- Get daily transaction summary
-- SELECT DATE(transaction_date) as date,
--        transaction_type,
--        COUNT(*) as count,
--        SUM(amount) as total_amount
-- FROM transactions
-- WHERE status = 'COMPLETED'
-- GROUP BY DATE(transaction_date), transaction_type
-- ORDER BY date DESC;
