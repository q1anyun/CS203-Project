package com.chess.tms.user_service.dto;

import lombok.Data;

@Data
public class JwtRequest {
    private String username;
    private String password;
}
