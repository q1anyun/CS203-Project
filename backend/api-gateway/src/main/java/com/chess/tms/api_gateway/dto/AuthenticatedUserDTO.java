package com.chess.tms.user_service.dto;

import com.chess.tms.user_service.enums.UserRole;

import lombok.Data;

@Data
public class AuthenticatedUserDTO {
    private Long userId;

    private Long playerId;

    private String username;

    private String email;

    private UserRole role;
}
