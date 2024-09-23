package com.chess.tms.auth_service.dto;

import lombok.Data;

@Data
public class JwtResponse {
    private String token;

    private long expiresIn;

    public String getToken() {
        return token;
    }
}
