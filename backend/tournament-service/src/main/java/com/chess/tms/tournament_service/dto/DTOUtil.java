package com.chess.tms.tournament_service.dto;

import org.springframework.beans.factory.annotation.Autowired;

import com.chess.tms.tournament_service.model.Tournament;
import com.chess.tms.tournament_service.model.TournamentPlayer;
import com.chess.tms.tournament_service.repository.TournamentRepository;

public class DTOUtil {
        //helper functions
    public static TournamentDetailsDTO convertEntryToDTO(Tournament tournament) {
        TournamentDetailsDTO dto = new TournamentDetailsDTO();
        dto.setId(tournament.getTournamentId());
        dto.setName(tournament.getName());
        dto.setStartDate(tournament.getStartDate());
        dto.setEndDate(tournament.getEndDate());
        dto.setMinElo(tournament.getMinElo());
        dto.setMaxElo(tournament.getMaxElo());
        dto.setCurrentPlayers(tournament.getCurrentPlayers());
        dto.setMaxPlayers(tournament.getMaxPlayers());
        dto.setStatus(tournament.getStatus());
        dto.setTimeControl(tournament.getTimeControl());

        dto.setCreatorId(tournament.getCreatorId());

        return dto;
    }

    public static PlayerRegistrationDTO convertPlayerEntrytoPlayerDTO(TournamentPlayer player) {
        PlayerRegistrationDTO dto = new PlayerRegistrationDTO();
        dto.setPlayerId(player.getId());
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
        tournament.setMaxPlayers(dto.getMaxPlayers());

        return tournament;
    }
}
