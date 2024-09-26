package com.chess.tms.tournament_service.dto;

import com.chess.tms.tournament_service.model.Tournament;
import com.chess.tms.tournament_service.model.TournamentPlayer;

public class DTOUtil {
        //helper functions
    public static TournamentDetailsDTO convertEntryToDTO(Tournament tournament) {
        TournamentDetailsDTO dto = new TournamentDetailsDTO();
        dto.setId(tournament.getTournamentId());
        dto.setName(tournament.getName());
        dto.setCreatorId(tournament.getCreatorId());
        dto.setStartDate(tournament.getStartDate());
        dto.setEndDate(tournament.getEndDate());
        dto.setMinElo(tournament.getMinElo());
        dto.setMaxElo(tournament.getMaxElo());
        dto.setTotalPlayers(tournament.getTotalPlayers());
        dto.setStatus(tournament.getStatus());
        dto.setTimeControl(tournament.getTimeControl());

        return dto;
    }

    public static PlayerRegistrationDTO convertPlayerEntrytoPlayerDTO(TournamentPlayer player) {
        PlayerRegistrationDTO dto = new PlayerRegistrationDTO();
        dto.setPlayerId(player.getId());
        dto.setRegistrationStatus(player.getRegistrationStatus());
        dto.setTournamentId(player.getTournament().getTournamentId());

        return dto;
    }

    public static Tournament convertDTOToTournament(TournamentRegistrationDTO dto, long creatorId){
        Tournament tournament = new Tournament();
        tournament.setName(dto.getName());
        tournament.setCreatorId(creatorId);
        tournament.setStartDate(dto.getStartDate());
        tournament.setEndDate(dto.getEndDate());
        tournament.setMinElo(dto.getMinElo());
        tournament.setMaxElo(dto.getMaxElo());
        tournament.setTotalPlayers(dto.getTotalPlayers());
        tournament.setTimeControl(dto.getTimeControl());

        return tournament;
    }
}
