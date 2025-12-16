package com.enterprise.banking.repository;

import com.enterprise.banking.model.LibraryBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * DAO pattern implementation for LibraryBook entity.
 */
@Repository
public interface LibraryBookRepository extends JpaRepository<LibraryBook, Long> {

    /**
     * Find book by ISBN.
     */
    Optional<LibraryBook> findByIsbn(String isbn);

    /**
     * Find books by title (case-insensitive partial match).
     */
    @Query("SELECT b FROM LibraryBook b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<LibraryBook> searchByTitle(@Param("title") String title);

    /**
     * Find books by author (case-insensitive partial match).
     */
    @Query("SELECT b FROM LibraryBook b WHERE LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))")
    List<LibraryBook> searchByAuthor(@Param("author") String author);

    /**
     * Find books by category.
     */
    List<LibraryBook> findByCategory(String category);

    /**
     * Find books by status.
     */
    List<LibraryBook> findByStatus(LibraryBook.BookStatus status);

    /**
     * Find available books (availableCopies > 0).
     */
    @Query("SELECT b FROM LibraryBook b WHERE b.availableCopies > 0 " +
            "AND b.status = 'AVAILABLE'")
    List<LibraryBook> findAvailableBooks();

    /**
     * Find books by publisher.
     */
    List<LibraryBook> findByPublisher(String publisher);

    /**
     * Get all unique categories.
     */
    @Query("SELECT DISTINCT b.category FROM LibraryBook b ORDER BY b.category")
    List<String> findAllCategories();

    /**
     * Check if ISBN exists.
     */
    boolean existsByIsbn(String isbn);

    /**
     * Count books by category.
     */
    long countByCategory(String category);
}
