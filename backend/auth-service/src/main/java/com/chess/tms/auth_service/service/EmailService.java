package com.chess.tms.auth_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service for sending email notifications, specifically OTP emails, to users.
 * Uses Spring's JavaMailSender to send email messages.
 */
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Sends an OTP email to the specified email address.
     *
     * @param to  the recipient's email address
     * @param otp the one-time password (OTP) to be included in the email content
     */
    public void sendOtpEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("One-Time Password for Your Chess MVP Account");

        String emailContent = String.format(
                "Dear user,\n\n" +
                        "Your one-time passcode for account creation is: %s\n\n" +
                        "Thank you for choosing Chess MVP!\n\n" +
                        "Best regards,\n" +
                        "The Chess MVP Team",
                otp);

        message.setText(emailContent);
        mailSender.send(message);
    }
}
