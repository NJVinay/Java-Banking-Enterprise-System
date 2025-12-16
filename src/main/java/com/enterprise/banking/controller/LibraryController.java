package com.enterprise.banking.controller;

import com.enterprise.banking.model.LibraryBook;
import com.enterprise.banking.service.LibraryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for library management operations (MVC Pattern - Controller
 * layer).
 */
@Slf4j
@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;

    /**
     * Add a new book to the library.
     * POST /api/library/books
     */
    @PostMapping("/books")
    public ResponseEntity<LibraryBook> addBook(@RequestBody LibraryBook book) {
        log.info("REST API - Add book: {}", book.getTitle());

        try {
            LibraryBook savedBook = libraryService.addBook(book);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedBook);
        } catch (IllegalArgumentException e) {
            log.error("Add book failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Add book error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Borrow a book.
     * POST /api/library/books/{isbn}/borrow
     */
    @PostMapping("/books/{isbn}/borrow")
    public ResponseEntity<Void> borrowBook(@PathVariable String isbn) {
        log.info("REST API - Borrow book with ISBN: {}", isbn);

        try {
            libraryService.borrowBook(isbn);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Borrow book failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Borrow book error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Return a book.
     * POST /api/library/books/{isbn}/return
     */
    @PostMapping("/books/{isbn}/return")
    public ResponseEntity<Void> returnBook(@PathVariable String isbn) {
        log.info("REST API - Return book with ISBN: {}", isbn);

        try {
            libraryService.returnBook(isbn);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("Return book failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Return book error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Search books by keyword.
     * GET /api/library/books/search
     */
    @GetMapping("/books/search")
    public ResponseEntity<List<LibraryBook>> searchBooks(@RequestParam String keyword) {
        log.info("REST API - Search books with keyword: {}", keyword);

        try {
            List<LibraryBook> books = libraryService.searchBooks(keyword);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            log.error("Search books error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get available books.
     * GET /api/library/books/available
     */
    @GetMapping("/books/available")
    public ResponseEntity<List<LibraryBook>> getAvailableBooks() {
        log.info("REST API - Get available books");

        try {
            List<LibraryBook> books = libraryService.getAvailableBooks();
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            log.error("Get available books error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all categories.
     * GET /api/library/categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        log.info("REST API - Get all categories");

        try {
            List<String> categories = libraryService.getAllCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            log.error("Get categories error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
