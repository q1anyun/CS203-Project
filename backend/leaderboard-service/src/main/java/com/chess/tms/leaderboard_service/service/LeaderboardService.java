package com.chess.tms.leaderboard_service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chess.tms.leaderboard_service.repository.LeaderboardRepository;
import com.chess.tms.leaderboard_service.dto.LeaderboardDTO;
import com.chess.tms.leaderboard_service.dto.LeaderboardRequestDTO;
import com.chess.tms.leaderboard_service.exception.EntryAlreadyExistsException;
import com.chess.tms.leaderboard_service.exception.EntryDoesNotExistException;
import com.chess.tms.leaderboard_service.model.LeaderboardEntry;
import com.chess.tms.leaderboard_service.dto.DTOUtil;

@Service 
public class LeaderboardService {
    
    @Autowired
    public LeaderboardRepository leaderboardRepository;

    public List<LeaderboardDTO> findAllRankings() {
        List<LeaderboardEntry> list = leaderboardRepository.findAll();
        return DTOUtil.convertEntryListToDTOList(list);
    }

    public LeaderboardDTO findSpecificEntry(long playerId) {
        Optional<LeaderboardEntry> entry = leaderboardRepository.findByPlayerId(playerId);
        if (entry.isEmpty()) {
            throw new EntryDoesNotExistException("Entry with player id:  " + playerId + " exists");
        }

        return DTOUtil.convertEntryToDTO(entry.get());
    }

    public List<LeaderboardDTO> findByEloBetween(int minElo, int maxElo) {
        List<LeaderboardEntry> list = leaderboardRepository.findByEloBetween(minElo, maxElo);
        return DTOUtil.convertEntryListToDTOList(list);
    }

    public List<LeaderboardDTO> findByEloGreaterThanEqual(int minElo) {
        List<LeaderboardEntry> list = leaderboardRepository.findByEloGreaterThanEqual(minElo);
        return DTOUtil.convertEntryListToDTOList(list);
    }

    public List<LeaderboardDTO> findByEloLessThanEqual(int maxElo) {
        List<LeaderboardEntry> list = leaderboardRepository.findByEloLessThanEqual(maxElo);
        return DTOUtil.convertEntryListToDTOList(list);
    }

    public List<LeaderboardDTO> findByRankingBetween(int minRank, int maxRank) {
        List<LeaderboardEntry> list = leaderboardRepository.findByRankingBetween(minRank, maxRank);
        return DTOUtil.convertEntryListToDTOList(list);
    }

    public List<LeaderboardDTO> findByRankingGreaterThanEqual(int minRank) {
        List<LeaderboardEntry> list = leaderboardRepository.findByRankingGreaterThanEqual(minRank);
        return DTOUtil.convertEntryListToDTOList(list);
    }

    public List<LeaderboardDTO> findByRankingLessThanEqual(int maxRank) {
        List<LeaderboardEntry> list = leaderboardRepository.findByEloLessThanEqual(maxRank);
        return DTOUtil.convertEntryListToDTOList(list);
    }

    public LeaderboardDTO saveEntry(LeaderboardRequestDTO dto) {
        Optional<LeaderboardEntry> entry = leaderboardRepository.findByPlayerId(dto.getPlayerId());

        if (leaderboardRepository.findByPlayerId(dto.getPlayerId()).isPresent()) {
            throw new EntryAlreadyExistsException("Entry with player id:  " + dto.getPlayerId() + " exists");
        }
        leaderboardRepository.save(DTOUtil.convertDTOToEntry(dto, entry.get().getId()));

        LeaderboardDTO saved = (LeaderboardDTO) dto;
        saved.setId(entry.get().getId());

        return saved;
    }

    public LeaderboardDTO deleteEntry(long playerId) {
        if (leaderboardRepository.findById(playerId).isEmpty()) {
            throw new EntryDoesNotExistException("Entry with playerId " + playerId + " does not exist");
        }

        LeaderboardDTO deleted = DTOUtil.convertEntryToDTO(leaderboardRepository.findByPlayerId(playerId).get());
        
        leaderboardRepository.deleteByPlayerId(playerId);

        return deleted;
    }

    public LeaderboardDTO updateEntry(LeaderboardDTO updateDTO) {
        Optional<LeaderboardEntry> entry = leaderboardRepository.findByPlayerId(updateDTO.getPlayerId());

        if (entry.isEmpty()) {
            throw new EntryDoesNotExistException("Entry with playerId " + updateDTO.getPlayerId() + " does not exist");
        }

        LeaderboardEntry updatedEntry = new LeaderboardEntry();
        updatedEntry.setId(entry.get().getId());
        updatedEntry.setPlayerId(entry.get().getPlayerId());
        updatedEntry.setElo(entry.get().getElo());
        updatedEntry.setRanking(entry.get().getRanking());
        updatedEntry.setLastUpdated(LocalDateTime.now());

        leaderboardRepository.save(updatedEntry);

        return DTOUtil.convertEntryToDTO(updatedEntry);
    }


    
}
