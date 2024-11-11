package com.chess.tms.auth_service.integration;

import com.chess.tms.auth_service.enums.UserRole;
import com.chess.tms.auth_service.exception.OtpValidationException;
import com.chess.tms.auth_service.exception.UserAlreadyExistsException;
import com.chess.tms.auth_service.model.OtpEntity;
import com.chess.tms.auth_service.model.User;
import com.chess.tms.auth_service.repository.OtpRepository;
import com.chess.tms.auth_service.repository.UsersRepository;
import com.chess.tms.auth_service.service.EmailService;
import com.chess.tms.auth_service.service.OTPService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Integration tests for the OTP (One-Time Password) Service.
 * Tests OTP generation, validation, and expiration flows.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class OTPServiceIntegrationTest {

    // Constants for test data
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_USERNAME = "testuser";
    
    // Constants for validation
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRATION_MINUTES = 5;
    private static final String SUCCESS_MESSAGE = "OTP generated successfully";

    // Dependencies
    @Autowired
    private OTPService otpService;
    @Autowired
    private OtpRepository otpRepository;
    @Autowired
    private UsersRepository usersRepository;
    @MockBean
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        // Ensure clean state before each test
        otpRepository.deleteAll();
        usersRepository.deleteAll();
        // Configure mock email service behavior
        doNothing().when(emailService).sendOtpEmail(anyString(), anyString());
    }

    /**
     * Helper method to create a test user with basic attributes
     */
    private User createTestUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("password123");
        user.setRole(UserRole.PLAYER);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return usersRepository.save(user);
    }

    /**
     * Tests successful OTP generation for new user registration
     */
    @Test
    void sendOtp_NewUser_Success() {
        String result = otpService.sendOtp(TEST_USERNAME, TEST_EMAIL);

        // Verify success response and email sending
        assertEquals(SUCCESS_MESSAGE, result);
        verify(emailService).sendOtpEmail(eq(TEST_EMAIL), anyString());
        
        // Verify OTP properties
        Optional<OtpEntity> savedOtp = otpRepository.findByEmail(TEST_EMAIL);
        assertTrue(savedOtp.isPresent());
        assertValidOtpProperties(savedOtp.get());
    }

    /**
     * Tests OTP generation failure when username already exists
     */
    @Test
    void sendOtp_ExistingUsername_ThrowsException() {
        createTestUser(TEST_USERNAME, "other@example.com");

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, 
            () -> otpService.sendOtp(TEST_USERNAME, TEST_EMAIL)
        );
        assertEquals("Username already exists.", exception.getMessage());
    }

    @Test
    void sendOtp_ExistingEmail_ThrowsException() {

        createTestUser("otheruser", TEST_EMAIL);

        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class, 
            () -> otpService.sendOtp(TEST_USERNAME, TEST_EMAIL)
        );
        assertEquals("Email already exists.", exception.getMessage());
    }

    @Test
    void resendOtp_Success() {
        // Test resending OTP generates a new code and sends new email
        otpService.sendOtp(TEST_USERNAME, TEST_EMAIL);
        String firstOtp = otpRepository.findByEmail(TEST_EMAIL).get().getOtp();

        String result = otpService.resendOtp(TEST_USERNAME, TEST_EMAIL);

        assertEquals("OTP generated successfully", result);
        verify(emailService, times(2)).sendOtpEmail(eq(TEST_EMAIL), anyString());
        
        Optional<OtpEntity> savedOtp = otpRepository.findByEmail(TEST_EMAIL);
        assertTrue(savedOtp.isPresent());
        assertNotEquals(firstOtp, savedOtp.get().getOtp());
        assertEquals(6, savedOtp.get().getOtp().length());
    }

    @Test
    void validateOtp_ValidOtp_Success() {
        // Test successful OTP validation and verify it's deleted after validation
        otpService.sendOtp(TEST_USERNAME, TEST_EMAIL);
        String otp = otpRepository.findByEmail(TEST_EMAIL).get().getOtp();

        String result = otpService.validateOtp(TEST_EMAIL, otp);

        assertEquals("OTP is valid", result);
        assertFalse(otpRepository.findByEmail(TEST_EMAIL).isPresent(), 
            "OTP should be deleted after successful validation");
    }

    @Test
    void validateOtp_InvalidOtp_ThrowsException() {

        otpService.sendOtp(TEST_USERNAME, TEST_EMAIL);

        OtpValidationException exception = assertThrows(OtpValidationException.class, 
            () -> otpService.validateOtp(TEST_EMAIL, "invalid-otp")
        );
        assertEquals("Invalid OTP or OTP has expired.", exception.getMessage());
    }

    @Test
    void validateOtp_ExpiredOtp_ThrowsException() {
        // Test that expired OTPs are rejected
        otpService.sendOtp(TEST_USERNAME, TEST_EMAIL);
        OtpEntity otpEntity = otpRepository.findByEmail(TEST_EMAIL).get();
        otpEntity.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        otpRepository.save(otpEntity);

        OtpValidationException exception = assertThrows(OtpValidationException.class, 
            () -> otpService.validateOtp(TEST_EMAIL, otpEntity.getOtp())
        );
        assertEquals("Invalid OTP or OTP has expired.", exception.getMessage());
    }

    @Test
    void validateOtp_NonexistentEmail_ThrowsException() {
        
        OtpValidationException exception = assertThrows(OtpValidationException.class, 
            () -> otpService.validateOtp("nonexistent@example.com", "123456")
        );
        assertEquals("No OTP found for the provided email.", exception.getMessage());
    }

    @Test
    void generateOtp_ProducesValidFormat() {
        // Test that generated OTP meets format requirements (6 digits)
        otpService.sendOtp(TEST_USERNAME, TEST_EMAIL);
        String otp = otpRepository.findByEmail(TEST_EMAIL).get().getOtp();

        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"), "OTP should be 6 digits");
    }

    /**
     * Tests that OTP expiration is correctly set to 5 minutes from creation
     */
    @Test
    void otpExpiration_ShouldBeSetToFiveMinutes() {
        otpService.sendOtp(TEST_USERNAME, TEST_EMAIL);
        OtpEntity otpEntity = otpRepository.findByEmail(TEST_EMAIL).get();

        // Truncate to the nearest minute to avoid precision issues
        LocalDateTime expectedExpiry = otpEntity.getCreatedAt()
            .plusMinutes(OTP_EXPIRATION_MINUTES)
            .truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime actualExpiry = otpEntity.getExpiresAt()
            .truncatedTo(ChronoUnit.MINUTES);
            
        assertEquals(expectedExpiry, actualExpiry);
    }

    /**
     * Helper method to verify common OTP properties
     */
    private void assertValidOtpProperties(OtpEntity otp) {
        assertNotNull(otp.getOtp(), "OTP should not be null");
        assertEquals(OTP_LENGTH, otp.getOtp().length(), "OTP should be 6 digits");
        assertTrue(otp.getOtp().matches("\\d{6}"), "OTP should contain only digits");
        assertTrue(otp.getExpiresAt().isAfter(LocalDateTime.now()), 
            "OTP should not be expired");
    }
}