package com.enterprise.banking.repository;

import com.enterprise.banking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * DAO pattern implementation for User entity using Spring Data JPA.
 * Provides CRUD operations and custom queries for user management.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by username (case-sensitive).
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email (case-insensitive).
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Check if username exists.
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists.
     */
    boolean existsByEmail(String email);

    /**
     * Find all active users.
     */
    List<User> findByActiveTrue();

    /**
     * Find all locked users.
     */
    List<User> findByLockedTrue();

    /**
     * Find users by role.
     */
    List<User> findByRole(User.UserRole role);

    /**
     * Find users created after specified date.
     */
    List<User> findByCreatedAtAfter(LocalDateTime date);

    /**
     * Find users who haven't logged in recently.
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :cutoffDate OR u.lastLoginAt IS NULL")
    List<User> findInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Find users with failed login attempts above threshold.
     */
    @Query("SELECT u FROM User u WHERE u.failedLoginAttempts >= :threshold")
    List<User> findUsersWithFailedLogins(@Param("threshold") Integer threshold);

    /**
     * Count active users.
     */
    long countByActiveTrue();

    /**
     * Count users by role.
     */
    long countByRole(User.UserRole role);
}
