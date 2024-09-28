package com.chess.tms.auth_service.dto;

import lombok.Data;

@Data
public class PlayerRegistrationDTO {
    private Long userId;
    private String firstName;
    private String lastName;
    private String profilePicture;
}
