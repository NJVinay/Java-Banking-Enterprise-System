package com.enterprise.banking.repository;

import com.enterprise.banking.model.Customer;
import com.enterprise.banking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * DAO pattern implementation for Customer entity.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * Find customer by customer ID.
     */
    Optional<Customer> findByCustomerId(String customerId);

    /**
     * Find customer by user.
     */
    Optional<Customer> findByUser(User user);

    /**
     * Find customer by SSN.
     */
    Optional<Customer> findBySsn(String ssn);

    /**
     * Find customers by status.
     */
    List<Customer> findByStatus(Customer.CustomerStatus status);

    /**
     * Find customers with KYC completed.
     */
    List<Customer> findByKycCompletedTrue();

    /**
     * Find customers with KYC pending.
     */
    List<Customer> findByKycCompletedFalse();

    /**
     * Find customers by first and last name.
     */
    @Query("SELECT c FROM Customer c WHERE c.firstName = :firstName " +
            "AND c.lastName = :lastName")
    List<Customer> findByName(
            @Param("firstName") String firstName,
            @Param("lastName") String lastName);

    /**
     * Search customers by name pattern.
     */
    @Query("SELECT c FROM Customer c WHERE LOWER(c.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Customer> searchByName(@Param("name") String name);

    /**
     * Check if SSN exists.
     */
    boolean existsBySsn(String ssn);

    /**
     * Check if customer ID exists.
     */
    boolean existsByCustomerId(String customerId);

    /**
     * Count active customers.
     */
    long countByStatus(Customer.CustomerStatus status);
}
