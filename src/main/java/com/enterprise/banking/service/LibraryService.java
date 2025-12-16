package com.enterprise.banking.service;

import com.enterprise.banking.model.LibraryBook;
import com.enterprise.banking.repository.LibraryBookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for library management operations.
 * Demonstrates Java 8 Streams and Lambda expressions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryService {
    
    private final LibraryBookRepository bookRepository;
    
    @Transactional
    public LibraryBook addBook(LibraryBook book) {
        log.info("Adding new book: {}", book.getTitle());
        
        if (bookRepository.existsByIsbn(book.getIsbn())) {
            throw new IllegalArgumentException("Book with ISBN already exists: " + book.getIsbn());
        }
        
        return bookRepository.save(book);
    }
    
    @Transactional
    public void borrowBook(String isbn) {
        log.info("Borrowing book with ISBN: {}", isbn);
        
        LibraryBook book = bookRepository.findByIsbn(isbn)
            .orElseThrow(() -> new IllegalArgumentException("Book not found: " + isbn));
        
        book.borrowBook();
        bookRepository.save(book);
        
        log.info("Book borrowed successfully: {}", book.getTitle());
    }
    
    @Transactional
    public void returnBook(String isbn) {
        log.info("Returning book with ISBN: {}", isbn);
        
        LibraryBook book = bookRepository.findByIsbn(isbn)
            .orElseThrow(() -> new IllegalArgumentException("Book not found: " + isbn));
        
        book.returnBook();
        bookRepository.save(book);
        
        log.info("Book returned successfully: {}", book.getTitle());
    }
    
    @Transactional(readOnly = true)
    public List<LibraryBook> searchBooks(String keyword) {
        log.info("Searching books with keyword: {}", keyword);
        
        // Using Java 8 Streams to combine search results
        List<LibraryBook> titleResults = bookRepository.searchByTitle(keyword);
        List<LibraryBook> authorResults = bookRepository.searchByAuthor(keyword);
        
        return titleResults.stream()
            .distinct()
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<LibraryBook> getAvailableBooks() {
        return bookRepository.findAvailableBooks();
    }
    
    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return bookRepository.findAllCategories();
    }
}
