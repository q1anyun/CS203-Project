package com.chess.tms.user_service.dto;

import com.chess.tms.user_service.enums.UserRole;

import lombok.Data;

@Data
public class LoginResponse {
    private UserRole role;
    private JwtResponse jwtResponse;
}
