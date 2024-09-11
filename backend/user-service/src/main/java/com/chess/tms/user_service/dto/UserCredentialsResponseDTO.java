package com.chess.tms.user_service.dto;

import com.chess.tms.user_service.enums.UserRole;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCredentialsResponseDTO {

    private Long id;
    private String username;
    private String email;
    
    @Enumerated(EnumType.STRING)
    private UserRole role;
}