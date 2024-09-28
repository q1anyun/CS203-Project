package com.chess.tms.tournament_service.dto;

import com.chess.tms.tournament_service.enums.RegistrationStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerRegistrationDTO {

    private long playerId;

    private long tournamentId;
}