package com.chess.tms.auth_service.dto;

import lombok.Data;

@Data
public class PlayerRegistrationDTO {
    private long userId;
    private String firstName;
    private String lastName;
    private String profilePicture;
}
