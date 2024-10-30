package com.chess.tms.tournament_service.dto;

import com.chess.tms.tournament_service.enums.Format;
import com.chess.tms.tournament_service.model.Tournament;

public class DTOUtil {

    public static Tournament convertDTOToTournament(TournamentRegistrationDTO dto, long creatorId){
        Tournament tournament = new Tournament();
        tournament.setName(dto.getName());
        tournament.setCreatorId(creatorId);
        tournament.setStartDate(dto.getStartDate());
        tournament.setEndDate(dto.getEndDate());
        tournament.setMinElo(dto.getMinElo());
        tournament.setMaxElo(dto.getMaxElo());
        tournament.setMaxPlayers(dto.getMaxPlayers());

        tournament.setDescription(dto.getDescription());
        tournament.setPhoto(dto.getPhoto());
        if (dto.getFormat() != null && !dto.getFormat().isEmpty()) {
            String formatUpperCase = dto.getFormat().toUpperCase();
            if (formatUpperCase.equals("ONLINE") || formatUpperCase.equals("HYBRID") || formatUpperCase.equals("PHYSICAL")) {
                tournament.setFormat(Format.valueOf(formatUpperCase));
            } else {
                tournament.setFormat(Format.ONLINE);
            }
        } else {
            tournament.setFormat(Format.ONLINE);
        }
        tournament.setCountry(dto.getCountry());
        tournament.setLocationAddress(dto.getLocationAddress());
        tournament.setLocationLatitude(dto.getLocationLatitude());
        tournament.setLocationLongitude(dto.getLocationLongitude());

        return tournament;
    }
}
