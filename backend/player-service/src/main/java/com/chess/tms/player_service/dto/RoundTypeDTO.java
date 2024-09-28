package com.chess.tms.player_service.dto;
import lombok.Data;

@Data
public class RoundTypeDTO {
    private Long id;
    private String roundName; 
    private Integer numberOfPlayers;
}
