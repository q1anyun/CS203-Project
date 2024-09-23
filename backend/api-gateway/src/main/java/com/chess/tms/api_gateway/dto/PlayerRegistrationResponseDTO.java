package com.chess.tms.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerRegistrationResponseDTO {
    private String firstName;
    private String lastName;
}