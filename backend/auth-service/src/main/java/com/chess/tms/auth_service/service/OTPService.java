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

    public String sendOtp(String username, String email) {
        System.out.println("Checking if username exists...");
        if (usersRepository.findByUsername(username).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists.");
        }
        System.out.println("Checking if email exists...");
        if (usersRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists.");
        }

        String otp = generateOtp();
        OtpEntity otpEntity = new OtpEntity();
        otpEntity.setEmail(email);
        otpEntity.setOtp(otp);
        otpEntity.setCreatedAt(LocalDateTime.now());
        otpEntity.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otpRepository.save(otpEntity);
        emailService.sendOtpEmail(email, otp);

        return "OTP generated successfully";
    }

    public String resendOtp(String username, String email) {
        String otp = generateOtp();

        Optional<OtpEntity> existingOtpEntityOpt = otpRepository.findByEmail(email);

        OtpEntity otpEntity;
        if (existingOtpEntityOpt.isPresent()) {
            otpEntity = existingOtpEntityOpt.get();
            otpEntity.setOtp(otp);
            otpEntity.setCreatedAt(LocalDateTime.now());
            otpEntity.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        } else {
            otpEntity = new OtpEntity();
            otpEntity.setEmail(email);
            otpEntity.setOtp(otp);
            otpEntity.setCreatedAt(LocalDateTime.now());
            otpEntity.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        }

        otpRepository.save(otpEntity);

        emailService.sendOtpEmail(email, otp);

        return "OTP generated successfully";
    }

    private String generateOtp() {
        int randomNum = secureRandom.nextInt(1000000);
        return String.format("%06d", randomNum);
    }

    public String validateOtp(String email, String otp) {
        Optional<OtpEntity> otpEntityOpt = otpRepository.findByEmail(email);
    
        if (otpEntityOpt.isPresent()) {
            OtpEntity otpEntity = otpEntityOpt.get();
    
            if (otpEntity.getOtp().equals(otp) && LocalDateTime.now().isBefore(otpEntity.getExpiresAt())) {
                otpRepository.delete(otpEntity);
                return "OTP is valid"; 
            } else {
                throw new OtpValidationException("Invalid OTP or OTP has expired.");
            }
        } else {
            throw new OtpValidationException("No OTP found for the provided email.");
        }
    }
}
