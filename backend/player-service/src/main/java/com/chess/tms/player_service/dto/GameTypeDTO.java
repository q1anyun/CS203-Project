package com.chess.tms.player_service.dto;
import lombok.Data;

@Data
public class GameTypeDTO {
    private Long id;
    private String name;
    private Integer timeControlMinutes;  
}
