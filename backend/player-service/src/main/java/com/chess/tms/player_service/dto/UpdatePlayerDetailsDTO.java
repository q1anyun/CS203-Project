package com.chess.tms.player_service.dto;

import lombok.Data;

@Data
public class UpdatePlayerDetailsDTO {
    private String firstName;
    private String lastName;
    private String profilePicture;
    private String country;
}
