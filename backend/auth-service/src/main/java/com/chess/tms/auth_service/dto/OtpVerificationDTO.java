package com.chess.tms.auth_service.dto;

import lombok.Data;

@Data
public class OtpVerificationDTO {
    private String email;
    private String otp;
}