package com.chess.tms.auth_service.dto;

import lombok.Data;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class JwtRequest {
    private String username;
    private String password;
}
