package com.chess.tms.auth_service.dto;

import com.chess.tms.auth_service.enums.UserRole;

import lombok.Data;

@Data
public class LoginResponse {
    private UserRole role;
    private JwtResponse jwtResponse;
}
