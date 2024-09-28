package com.chess.tms.user_service.dto;

import com.chess.tms.user_service.enums.UserRole;

import lombok.Data;

@Data
public class UpdateUserRequestDTO {
    private String email;
    private String password;
    private UserRole role;
}
