package com.enterprise.banking.service;

import com.enterprise.banking.model.User;
import com.enterprise.banking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * JUnit tests for AuthenticationService.
 * Tests BCrypt password hashing and authentication logic.
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private AuthenticationService authenticationService;
    
    private User testUser;
    private BCryptPasswordEncoder passwordEncoder;
    
    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(12);
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("TestPass123!"));
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
        testUser.setRole(User.UserRole.CUSTOMER);
        testUser.setActive(true);
        testUser.setLocked(false);
        testUser.setFailedLoginAttempts(0);
    }
    
    @Test
    void testRegisterUser_Success() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        
        // Act
        User registeredUser = authenticationService.registerUser(
            "newuser",
            "SecurePass123!",
            "newuser@example.com",
            "New User",
            User.UserRole.CUSTOMER
        );
        
        // Assert
        assertNotNull(registeredUser);
        assertEquals("newuser", registeredUser.getUsername());
        assertEquals("newuser@example.com", registeredUser.getEmail());
        assertTrue(registeredUser.getActive());
        assertFalse(registeredUser.getLocked());
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    void testRegisterUser_UsernameExists_ThrowsException() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(true);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.registerUser(
                "existinguser",
                "Pass123!",
                "email@example.com",
                "User",
                User.UserRole.CUSTOMER
            );
        });
    }
    
    @Test
    void testRegisterUser_WeakPassword_ThrowsException() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.registerUser(
                "newuser",
                "weak",  // Too short
                "email@example.com",
                "User",
                User.UserRole.CUSTOMER
            );
        });
    }
    
    @Test
    void testAuthenticate_Success() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // Act
        User authenticatedUser = authenticationService.authenticate("testuser", "TestPass123!");
        
        // Assert
        assertNotNull(authenticatedUser);
        assertEquals("testuser", authenticatedUser.getUsername());
        assertEquals(0, authenticatedUser.getFailedLoginAttempts());
        assertNotNull(authenticatedUser.getLastLoginAt());
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    void testAuthenticate_InvalidPassword_IncrementsFailedAttempts() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.authenticate("testuser", "WrongPassword!");
        });
        
        assertEquals(1, testUser.getFailedLoginAttempts());
        verify(userRepository, times(1)).save(testUser);
    }
    
    @Test
    void testAuthenticate_LockedAccount_ThrowsException() {
        // Arrange
        testUser.setLocked(true);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        
        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            authenticationService.authenticate("testuser", "TestPass123!");
        });
    }
    
    @Test
    void testAuthenticate_InactiveAccount_ThrowsException() {
        // Arrange
        testUser.setActive(false);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        
        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            authenticationService.authenticate("testuser", "TestPass123!");
        });
    }
    
    @Test
    void testChangePassword_Success() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // Act
        authenticationService.changePassword("testuser", "TestPass123!", "NewPass456!");
        
        // Assert
        verify(userRepository, times(2)).save(any(User.class)); // Once for auth, once for password change
    }
    
    @Test
    void testIsPasswordStrong() {
        // Test strong password
        assertTrue(authenticationService.isPasswordStrong("StrongPass123!"));
        
        // Test weak passwords
        assertFalse(authenticationService.isPasswordStrong("short"));
        assertFalse(authenticationService.isPasswordStrong("NoDigits!"));
        assertFalse(authenticationService.isPasswordStrong("nouppercse123!"));
        assertFalse(authenticationService.isPasswordStrong("NOLOWERCASE123!"));
        assertFalse(authenticationService.isPasswordStrong("NoSpecialChar123"));
    }
    
    @Test
    void testUnlockAccount_Success() {
        // Arrange
        testUser.setLocked(true);
        testUser.setFailedLoginAttempts(5);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // Act
        authenticationService.unlockAccount("testuser");
        
        // Assert
        assertFalse(testUser.getLocked());
        assertEquals(0, testUser.getFailedLoginAttempts());
        verify(userRepository, times(1)).save(testUser);
    }
}
