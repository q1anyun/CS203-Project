package com.chess.tms.elo_service.exception.dto;

import java.util.ArrayList;
import java.util.List;

import com.chess.tms.elo_service.model.EloHistory;

public class DTOUtil {
    public static List<EloDTO> convertEntriesToDTOs(List<EloHistory> list) {
        List<EloDTO> dtoList = new ArrayList<>();
        for(EloHistory e : list) {
            EloDTO dto = new EloDTO();
            dto.setId(e.getId());
            dto.setPlayerId(e.getPlayerId());
            dto.setCreatedAt(e.getCreatedAt());
            dto.setOldElo(e.getOldElo());
            dto.setNewElo(e.getNewElo());
            dto.setChangeReason(e.getChangeReason());
            dtoList.add(dto);
        }

        return dtoList;
    }

    public static EloResponseDTO convertEntryToResponseDTO(EloHistory eloHistory) {
        EloResponseDTO eloDTO = new EloResponseDTO();
        
        eloDTO.setPlayerId(eloHistory.getPlayerId());
        eloDTO.setOldElo(eloHistory.getOldElo());
        eloDTO.setNewElo(eloHistory.getNewElo());
        eloDTO.setCreatedAt(eloHistory.getCreatedAt());
        eloDTO.setChangeReason(eloHistory.getChangeReason());
        return eloDTO;
    }

    public static List<EloResponseDTO> convertEntriesToResponseDTOs(List<EloHistory> list) {
        List<EloResponseDTO> responses = new ArrayList<>();
        for(EloHistory e : list) {
            responses.add(convertEntryToResponseDTO(e));
        }

        return responses;
    }
}
