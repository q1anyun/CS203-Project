package com.chess.tms.auth_service.unit;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class OTPServiceTest {

    @Mock
    private EmailService emailService;

    @Mock
    private OtpRepository otpRepository;

    @Mock
    private UsersRepository usersRepository;

    @InjectMocks
    private OTPService otpService;

    // Common test variables
    private String email;
    private String otp;
    private OtpEntity otpEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        email = "testuser@example.com";
        otp = "123456";

        otpEntity = new OtpEntity();
        otpEntity.setEmail(email);
        otpEntity.setOtp(otp);
        otpEntity.setCreatedAt(LocalDateTime.now());
        otpEntity.setExpiresAt(LocalDateTime.now().plusMinutes(5));
    }

    @Test
    void testSendOtp_Success() {
        // Arrange
        String username = "testuser";
        when(usersRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(usersRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        String result = otpService.sendOtp(username, email);

        // Assert
        assertEquals("OTP generated successfully", result);
        verify(otpRepository, times(1)).save(any(OtpEntity.class));
        verify(emailService, times(1)).sendOtpEmail(eq(email), anyString());
    }

    @Test
    void testSendOtp_UserAlreadyExists() {
        // Arrange
        String username = "existinguser";
        when(usersRepository.findByUsername(username)).thenReturn(Optional.of(new User()));

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> otpService.sendOtp(username, email));
        verify(otpRepository, never()).save(any(OtpEntity.class));
        verify(emailService, never()).sendOtpEmail(anyString(), anyString());
    }

    @Test
    void testSendOtp_EmailAlreadyExists() {
        // Arrange
        String username = "newuser";
        when(usersRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(usersRepository.findByEmail(email)).thenReturn(Optional.of(new User()));

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> otpService.sendOtp(username, email));
        verify(otpRepository, never()).save(any(OtpEntity.class));
        verify(emailService, never()).sendOtpEmail(anyString(), anyString());
    }

    @Test
    void testResendOtp_Success() {
        // Arrange
        when(otpRepository.findByEmail(email)).thenReturn(Optional.of(otpEntity));

        // Act
        String result = otpService.resendOtp("testuser", email);

        // Assert
        assertEquals("OTP generated successfully", result);
        verify(otpRepository, times(1)).save(any(OtpEntity.class));
        verify(emailService, times(1)).sendOtpEmail(eq(email), anyString());
    }

    @Test
    void testResendOtp_NewOtpEntity() {
        // Arrange
        when(otpRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        String result = otpService.resendOtp("newuser", email);

        // Assert
        assertEquals("OTP generated successfully", result);
        verify(otpRepository, times(1)).save(any(OtpEntity.class));
        verify(emailService, times(1)).sendOtpEmail(eq(email), anyString());
    }

    @Test
    void testValidateOtp_Success() {
        // Arrange
        when(otpRepository.findByEmail(email)).thenReturn(Optional.of(otpEntity));

        // Act
        String result = otpService.validateOtp(email, otp);

        // Assert
        assertEquals("OTP is valid", result);
        verify(otpRepository, times(1)).delete(otpEntity);
    }

    @Test
    void testValidateOtp_InvalidOtp() {
        // Arrange
        when(otpRepository.findByEmail(email)).thenReturn(Optional.of(otpEntity));

        // Act & Assert
        assertThrows(OtpValidationException.class, () -> otpService.validateOtp(email, "wrongOtp"));
        verify(otpRepository, never()).delete(any(OtpEntity.class));
    }

    @Test
    void testValidateOtp_ExpiredOtp() {
        // Arrange
        otpEntity.setExpiresAt(LocalDateTime.now().minusMinutes(1)); // Expired OTP
        when(otpRepository.findByEmail(email)).thenReturn(Optional.of(otpEntity));

        // Act & Assert
        assertThrows(OtpValidationException.class, () -> otpService.validateOtp(email, otp));
        verify(otpRepository, never()).delete(any(OtpEntity.class));
    }

    @Test
    void testValidateOtp_NoOtpFound_ThrowsOtpValidationException() {
        // Arrange
        when(otpRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(OtpValidationException.class, () -> otpService.validateOtp(email, otp),
                "No OTP found for the provided email.");
        verify(otpRepository, never()).delete(any(OtpEntity.class)); // Ensure no deletion happens
    }
}
