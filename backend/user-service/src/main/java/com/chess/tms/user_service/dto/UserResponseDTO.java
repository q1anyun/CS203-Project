package com.chess.tms.user_service.dto;

import com.chess.tms.user_service.enums.UserRole;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserResponseDTO {
        private String username;
        private String email;
        private UserRole role;
}
