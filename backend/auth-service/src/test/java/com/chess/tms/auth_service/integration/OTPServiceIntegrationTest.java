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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class OTPServiceIntegrationTest {

    @Autowired
    private OTPService otpService;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private UsersRepository usersRepository;

    @MockBean
    private EmailService emailService;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        otpRepository.deleteAll();
        usersRepository.deleteAll();
        doNothing().when(emailService).sendOtpEmail(anyString(), anyString());
    }

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

    @Test
    void sendOtp_NewUser_Success() {
     
        String result = otpService.sendOtp(TEST_USERNAME, TEST_EMAIL);

     
        assertEquals("OTP generated successfully", result);
        verify(emailService, times(1)).sendOtpEmail(eq(TEST_EMAIL), anyString());
        
        Optional<OtpEntity> savedOtp = otpRepository.findByEmail(TEST_EMAIL);
        assertTrue(savedOtp.isPresent());
        assertNotNull(savedOtp.get().getOtp());
        assertTrue(savedOtp.get().getExpiresAt().isAfter(LocalDateTime.now()));
        assertEquals(6, savedOtp.get().getOtp().length());
    }

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
       
        otpService.sendOtp(TEST_USERNAME, TEST_EMAIL);
        String otp = otpRepository.findByEmail(TEST_EMAIL).get().getOtp();

      
        assertNotNull(otp);
        assertEquals(6, otp.length());
        assertTrue(otp.matches("\\d{6}"), "OTP should be 6 digits");
    }

    @Test
    void otpExpiration_ShouldBeSetToFiveMinutes() {
   
        otpService.sendOtp(TEST_USERNAME, TEST_EMAIL);
        OtpEntity otpEntity = otpRepository.findByEmail(TEST_EMAIL).get();

      
        LocalDateTime expectedExpiry = otpEntity.getCreatedAt().plusMinutes(5);
        assertEquals(expectedExpiry, otpEntity.getExpiresAt());
    }
}