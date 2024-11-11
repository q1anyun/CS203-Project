package com.chess.tms.auth_service.controller;

import com.chess.tms.auth_service.dto.OtpRequest;
import com.chess.tms.auth_service.dto.OtpVerificationDTO;
import com.chess.tms.auth_service.exception.OtpValidationException;
import com.chess.tms.auth_service.service.OTPService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/otp")
public class OTPController {

    @Autowired
    private OTPService otpService;

    /**
     * Sends a one-time password (OTP) to the user's email for verification
     * purposes.
     *
     * @param otpRequest the request object containing the user's username and email
     *                   (validated)
     * @return a ResponseEntity containing a success message if the OTP is sent
     *         successfully
     */
    @PostMapping("/request")
    public ResponseEntity<String> sendOtp(@Valid @RequestBody OtpRequest otpRequest) {
        return ResponseEntity.ok(otpService.sendOtp(otpRequest.getUsername(), otpRequest.getEmail()));
    }

    /**
     * Validates the OTP provided by the user for verification purposes.
     *
     * @param otpVerificationDTO the DTO containing the user's email and OTP for
     *                           validation
     * @return a ResponseEntity containing a success message if the OTP is valid
     * @throws OtpValidationException if the OTP is invalid or expired
     */
    @PostMapping("/verification")
    public ResponseEntity<String> validateOtp(@RequestBody OtpVerificationDTO otpVerificationDTO) {
        return ResponseEntity.ok(otpService.validateOtp(otpVerificationDTO.getEmail(), otpVerificationDTO.getOtp()));
    }

    /**
     * Resends the OTP to the user's email if they did not receive it or if it
     * expired.
     *
     * @param otpRequest the request object containing the user's username and email
     * @return a ResponseEntity containing a success message if the OTP is resent
     *         successfully
     */
    @PostMapping("/retry")
    public ResponseEntity<String> resendOtp(@RequestBody OtpRequest otpRequest) {
        return ResponseEntity.ok(otpService.resendOtp(otpRequest.getUsername(), otpRequest.getEmail()));
    }

}
