package com.enterprise.banking.service;

import com.enterprise.banking.model.User;
import com.enterprise.banking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Authentication service with BCrypt password hashing.
 * Implements secure user authentication and account management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
    
    /**
     * Register a new user with BCrypt password hashing.
     */
    @Transactional
    public User registerUser(String username, String password, String email, 
                            String fullName, User.UserRole role) {
        log.info("Registering new user: {}", username);
        
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        
        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }
        
        // Create user with hashed password
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // BCrypt hashing
        user.setEmail(email);
        user.setFullName(fullName);
        user.setRole(Optional.ofNullable(role).orElse(User.UserRole.CUSTOMER));
        user.setActive(true);
        user.setLocked(false);
        user.setFailedLoginAttempts(0);
        
        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {} with role: {}", username, savedUser.getRole());
        
        return savedUser;
    }
    
    /**
     * Authenticate user with BCrypt password verification.
     */
    @Transactional
    public User authenticate(String username, String password) {
        log.info("Authentication attempt for user: {}", username);
        
        // Find user
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        
        // Check if account is locked
        if (user.getLocked()) {
            log.warn("Authentication failed - Account locked: {}", username);
            throw new IllegalStateException("Account is locked. Please contact support.");
        }
        
        // Check if account is active
        if (!user.getActive()) {
            log.warn("Authentication failed - Account inactive: {}", username);
            throw new IllegalStateException("Account is not active");
        }
        
        // Verify password using BCrypt
        if (!passwordEncoder.matches(password, user.getPassword())) {
            user.incrementFailedLoginAttempts();
            userRepository.save(user);
            
            log.warn("Authentication failed - Invalid password for user: {} (Attempt: {})", 
                    username, user.getFailedLoginAttempts());
            
            if (user.getLocked()) {
                throw new IllegalStateException(
                    "Account locked due to too many failed login attempts");
            }
            
            throw new IllegalArgumentException("Invalid username or password");
        }
        
        // Successful authentication
        user.recordSuccessfulLogin();
        userRepository.save(user);
        
        log.info("Authentication successful for user: {}", username);
        return user;
    }
    
    /**
     * Change user password with BCrypt hashing.
     */
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        log.info("Password change request for user: {}", username);
        
        // Authenticate with old password
        User user = authenticate(username, oldPassword);
        
        // Validate new password
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters");
        }
        
        // Check if new password is different from old
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from old password");
        }
        
        // Update password with BCrypt hashing
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("Password changed successfully for user: {}", username);
    }
    
    /**
     * Unlock a locked account (admin function).
     */
    @Transactional
    public void unlockAccount(String username) {
        log.info("Unlocking account: {}", username);
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        user.resetFailedLoginAttempts();
        userRepository.save(user);
        
        log.info("Account unlocked successfully: {}", username);
    }
    
    /**
     * Deactivate user account.
     */
    @Transactional
    public void deactivateAccount(String username) {
        log.info("Deactivating account: {}", username);
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        
        user.setActive(false);
        userRepository.save(user);
        
        log.info("Account deactivated successfully: {}", username);
    }
    
    /**
     * Validate password strength.
     */
    public boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(ch -> 
            "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(ch) >= 0);
        
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}
