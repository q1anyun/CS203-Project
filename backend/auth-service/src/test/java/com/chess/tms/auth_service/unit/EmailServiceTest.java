package com.chess.tms.auth_service.unit;

import com.chess.tms.auth_service.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.*;

class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendOtpEmail() {
        // Arrange
        String to = "testuser@example.com";
        String otp = "123456";

        // Act
        emailService.sendOtpEmail(to, otp);

        // Assert
        verify(mailSender, times(1)).send(argThat((SimpleMailMessage message) -> 
            message.getTo()[0].equals(to) &&
            message.getSubject().equals("One-Time Password for Your Chess MVP Account") &&
            message.getText().contains("Your one-time passcode for account creation is: 123456") &&
            message.getText().contains("Thank you for choosing Chess MVP!")
        ));
    }
}
