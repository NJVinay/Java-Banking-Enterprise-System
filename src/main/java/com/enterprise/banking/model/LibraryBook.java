package com.enterprise.banking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Library entity for library management system module.
 */
@Entity
@Table(name = "library_books")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LibraryBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String isbn;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 100)
    private String author;

    @Column(length = 100)
    private String publisher;

    private LocalDate publicationDate;

    @Column(length = 50)
    private String category;

    @Column(nullable = false)
    private Integer totalCopies = 1;

    @Column(nullable = false)
    private Integer availableCopies = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookStatus status = BookStatus.AVAILABLE;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum BookStatus {
        AVAILABLE, BORROWED, RESERVED, LOST, DAMAGED
    }

    public boolean isAvailable() {
        return availableCopies > 0 && status == BookStatus.AVAILABLE;
    }

    public void borrowBook() {
        if (!isAvailable()) {
            throw new IllegalStateException("Book not available");
        }
        availableCopies--;
        if (availableCopies == 0) {
            status = BookStatus.BORROWED;
        }
    }

    public void returnBook() {
        availableCopies++;
        if (status == BookStatus.BORROWED && availableCopies > 0) {
            status = BookStatus.AVAILABLE;
        }
    }
}
