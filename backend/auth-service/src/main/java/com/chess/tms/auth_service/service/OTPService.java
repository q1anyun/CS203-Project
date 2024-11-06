package com.chess.tms.auth_service.service;

import com.chess.tms.auth_service.exception.OtpValidationException;
import com.chess.tms.auth_service.exception.UserAlreadyExistsException;
import com.chess.tms.auth_service.model.OtpEntity;
import com.chess.tms.auth_service.repository.OtpRepository;
import com.chess.tms.auth_service.repository.UsersRepository;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for handling OTP (One-Time Password) generation, validation, and
 * sending.
 * This service interacts with the user and OTP repositories to create and
 * verify OTPs.
 */
@Service
public class OTPService {

    private final EmailService emailService;
    private final OtpRepository otpRepository;
    private final UsersRepository usersRepository;

    public OTPService(EmailService emailService, OtpRepository otpRepository, UsersRepository usersRepository) {
        this.emailService = emailService;
        this.otpRepository = otpRepository;
        this.usersRepository = usersRepository;
    }

    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Sends an OTP for user registration or account recovery.
     * Verifies that the username and email do not already exist in the system.
     *
     * @param username the username to check for uniqueness
     * @param email    the email address to check for uniqueness
     * @return a success message indicating that the OTP was generated
     * @throws UserAlreadyExistsException if the username or email already exists
     */
    public String sendOtp(String username, String email) {
        // Check if the username exists in the system
        if (usersRepository.findByUsername(username).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists.");
        }

        // Check if the email exists in the system
        if (usersRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists.");
        }

        // Generate the OTP
        String otp = generateOtp();

        // Create a new OTP entity and set its properties
        OtpEntity otpEntity = new OtpEntity();
        otpEntity.setEmail(email);
        otpEntity.setOtp(otp);
        otpEntity.setCreatedAt(LocalDateTime.now());
        otpEntity.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        // Save the OTP entity to the database
        otpRepository.save(otpEntity);

        // Send the OTP email to the user
        emailService.sendOtpEmail(email, otp);

        return "OTP generated successfully";
    }

    /**
     * Resends an OTP if a previous OTP exists for the given email.
     * If no OTP exists, a new OTP is generated and sent to the email.
     *
     * @param username the username of the user requesting the OTP
     * @param email    the email address where the OTP will be sent
     * @return a success message indicating that the OTP was generated
     */
    public String resendOtp(String username, String email) {
        // Generate a new OTP
        String otp = generateOtp();

        // Try to find an existing OTP entity for the email
        Optional<OtpEntity> existingOtpEntityOpt = otpRepository.findByEmail(email);

        OtpEntity otpEntity;
        if (existingOtpEntityOpt.isPresent()) {
            // If an OTP already exists, update its details
            otpEntity = existingOtpEntityOpt.get();
            otpEntity.setOtp(otp);
            otpEntity.setCreatedAt(LocalDateTime.now());
            otpEntity.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        } else {
            // If no OTP exists, create a new one
            otpEntity = new OtpEntity();
            otpEntity.setEmail(email);
            otpEntity.setOtp(otp);
            otpEntity.setCreatedAt(LocalDateTime.now());
            otpEntity.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        }

        // Save the OTP entity to the database
        otpRepository.save(otpEntity);

        // Send the OTP email to the user
        emailService.sendOtpEmail(email, otp);

        return "OTP generated successfully";
    }

    /**
     * Generates a 6-digit OTP.
     *
     * @return a randomly generated 6-digit OTP
     */
    private String generateOtp() {
        int randomNum = secureRandom.nextInt(1000000);
        return String.format("%06d", randomNum);
    }

    /**
     * Validates the OTP for the given email address.
     * The OTP is considered valid if it matches and has not expired.
     *
     * @param email the email address associated with the OTP
     * @param otp   the OTP to validate
     * @return a success message indicating that the OTP is valid
     * @throws OtpValidationException if the OTP is invalid or expired
     */
    public String validateOtp(String email, String otp) {
        // Look for an OTP entity associated with the email
        Optional<OtpEntity> otpEntityOpt = otpRepository.findByEmail(email);

        if (otpEntityOpt.isPresent()) {
            OtpEntity otpEntity = otpEntityOpt.get();

            // Check if the OTP matches and has not expired
            if (otpEntity.getOtp().equals(otp) && LocalDateTime.now().isBefore(otpEntity.getExpiresAt())) {
                otpRepository.delete(otpEntity); // Delete the OTP after validation
                return "OTP is valid";
            } else {
                throw new OtpValidationException("Invalid OTP or OTP has expired.");
            }
        } else {
            throw new OtpValidationException("No OTP found for the provided email.");
        }
    }
}
