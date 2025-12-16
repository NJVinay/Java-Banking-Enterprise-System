package com.enterprise.banking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Customer entity representing banking customer profiles and KYC information.
 */
@Entity
@Table(name = "customers", indexes = {
        @Index(name = "idx_ssn", columnList = "ssn"),
        @Index(name = "idx_customer_id", columnList = "customerId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String customerId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(length = 50)
    private String middleName;

    @Column(unique = true, length = 11)
    private String ssn; // Social Security Number

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(length = 100)
    private String nationality;

    @Column(length = 20)
    private String idType; // PASSPORT, DRIVERS_LICENSE, etc.

    @Column(length = 50)
    private String idNumber;

    @Embedded
    private Address address;

    @Column(length = 15)
    private String phoneNumber;

    @Column(length = 15)
    private String alternatePhone;

    @Column(length = 100)
    private String emailAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerStatus status = CustomerStatus.ACTIVE;

    @Column(nullable = false)
    private Boolean kycCompleted = false;

    private LocalDate kycCompletedDate;

    @Column(length = 50)
    private String occupation;

    @Column(length = 100)
    private String employer;

    @Column(length = 50)
    private String annualIncome;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum Gender {
        MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
    }

    public enum CustomerStatus {
        ACTIVE, INACTIVE, SUSPENDED, BLACKLISTED
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {
        @Column(length = 200)
        private String street;

        @Column(length = 100)
        private String city;

        @Column(length = 100)
        private String state;

        @Column(length = 20)
        private String zipCode;

        @Column(length = 100)
        private String country;
    }

    public String getFullName() {
        return String.format("%s %s %s",
                firstName,
                middleName != null ? middleName : "",
                lastName).replaceAll("\\s+", " ").trim();
    }

    public boolean isActive() {
        return status == CustomerStatus.ACTIVE;
    }

    public int getAge() {
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }
}
