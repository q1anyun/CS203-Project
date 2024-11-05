package com.chess.tms.auth_service.controller;

import com.chess.tms.auth_service.dto.OtpRequest;
import com.chess.tms.auth_service.dto.OtpVerificationDTO;
import com.chess.tms.auth_service.service.OTPService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/otp")
public class OTPController {

    @Autowired
    private OTPService otpService;

    @PostMapping("/request")
    public ResponseEntity<String> sendOtp(@Valid @RequestBody OtpRequest otpRequest) {    
            return ResponseEntity.ok(otpService.sendOtp(otpRequest.getUsername(), otpRequest.getEmail()));
    }

    @PostMapping("/verification")
    public ResponseEntity<String> validateOtp(@RequestBody OtpVerificationDTO otpVerificationDTO) {
        return ResponseEntity.ok(otpService.validateOtp(otpVerificationDTO.getEmail(), otpVerificationDTO.getOtp()));
    }

    @PostMapping("/retry")
    public ResponseEntity<String> resendOtp(@RequestBody OtpRequest otpRequest) {
        return ResponseEntity.ok(otpService.resendOtp(otpRequest.getUsername(), otpRequest.getEmail()));
    }
    
}
