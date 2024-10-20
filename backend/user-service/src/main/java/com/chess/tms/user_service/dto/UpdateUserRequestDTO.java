package com.chess.tms.user_service.dto;

import com.chess.tms.user_service.enums.UserRole;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateUserRequestDTO {
    private String email;

    @NotBlank(message = "Password is required")
    private String oldPassword;

    private String newPassword;
    private UserRole role;
}
