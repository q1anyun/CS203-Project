package com.chess.tms.auth_service.dto;

import com.chess.tms.auth_service.enums.UserRole;

import lombok.Data;

@Data
public class AuthenticatedUserDTO {
    private Long userId;

    private Long playerId;

    private String username;

    private String email;

    private UserRole role;
}
