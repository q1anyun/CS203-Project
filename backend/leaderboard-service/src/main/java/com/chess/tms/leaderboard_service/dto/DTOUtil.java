package com.chess.tms.leaderboard_service.dto;

import java.util.ArrayList;
import java.util.List;

import com.chess.tms.leaderboard_service.model.LeaderboardEntry;

public class DTOUtil {
    
    public static LeaderboardDTO convertEntryToDTO(LeaderboardEntry entry) {
        LeaderboardDTO dto = new LeaderboardDTO();

        dto.setId(entry.getId());
        dto.setPlayerId(entry.getPlayerId());
        dto.setElo(entry.getElo());
        dto.setRanking(entry.getRanking());
        dto.setLastUpdated(entry.getLastUpdated());

        return dto;
    } 

    public static List<LeaderboardDTO> convertEntryListToDTOList(List<LeaderboardEntry> list) {
        List<LeaderboardDTO> dtoList = new ArrayList<>();
        for (LeaderboardEntry e : list) {
            dtoList.add(convertEntryToDTO(e));
        }

        return dtoList;
    }

    public static LeaderboardEntry convertDTOToEntry(LeaderboardRequestDTO dto, long entryId) {
        LeaderboardEntry entry = new LeaderboardEntry();

        entry.setId(entryId);
        entry.setPlayerId(dto.getPlayerId());
        entry.setElo(dto.getElo());
        entry.setRanking(dto.getRanking());
        entry.setLastUpdated(dto.getLastUpdated());

        return entry;
    } 
}
