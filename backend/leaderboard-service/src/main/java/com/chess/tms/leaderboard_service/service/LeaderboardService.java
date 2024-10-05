package com.chess.tms.leaderboard_service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chess.tms.leaderboard_service.repository.LeaderboardRepository;

import jakarta.transaction.Transactional;

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

    public void updateRankings(List<LeaderboardEntry> list, boolean isAsc) {
        int currRank = 1;
        int increment = 1;
        if (isAsc) {
            currRank = list.size();
            increment = -1;
        }
        for (LeaderboardEntry e : list) {
            e.setRanking(currRank);
            currRank += increment;
            leaderboardRepository.save(e);
        }
    }

    public int getRanking(List<LeaderboardEntry> list, LeaderboardEntry target) {
        int currRank = 1;
        for (LeaderboardEntry e : list) {
            if (e.getPlayerId() == target.getPlayerId()) {
                break;
            }
            currRank++;
        }
        return currRank;
    }

    public List<LeaderboardDTO> findAllRankingsAsc() {
        List<LeaderboardEntry> list = leaderboardRepository.findAllByOrderByEloDesc();
        updateRankings(list, true);
        return DTOUtil.convertEntryListToDTOList(list);
    }

    public List<LeaderboardDTO> findAllRankingsDesc() {
        List<LeaderboardEntry> list = leaderboardRepository.findAllByOrderByEloDesc();
        updateRankings(list, false);
        return DTOUtil.convertEntryListToDTOList(list);
    }

    public LeaderboardDTO findByPlayerId(long playerId) {
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

        if (entry.isPresent()) {
            throw new EntryAlreadyExistsException("Entry with player id: " + dto.getPlayerId() + " exists");
        }
        LeaderboardEntry newEntry = DTOUtil.convertDTOToEntry(dto, entry.get().getId());
        newEntry.setRanking(getRanking(leaderboardRepository.findAll(), newEntry));

        leaderboardRepository.save(newEntry);

        LeaderboardDTO saved = (LeaderboardDTO) dto;
        saved.setId(leaderboardRepository.findByPlayerId(dto.getPlayerId()).get().getId());
        saved.setLastUpdated(LocalDateTime.now());

        return saved;
    }

    @Transactional
    public LeaderboardDTO deleteEntry(long playerId) {
        if (leaderboardRepository.findByPlayerId(playerId).isEmpty()) {
            throw new EntryDoesNotExistException("Entry with playerId " + playerId + " does not exist");
        }

        LeaderboardDTO deleted = DTOUtil.convertEntryToDTO(leaderboardRepository.findByPlayerId(playerId).get());
        
        leaderboardRepository.deleteByPlayerId(playerId);

        return deleted;
    }

    public LeaderboardDTO updateEntry(LeaderboardRequestDTO updateDTO) {
        Optional<LeaderboardEntry> entry = leaderboardRepository.findByPlayerId(updateDTO.getPlayerId());

        if (entry.isEmpty()) {
            throw new EntryDoesNotExistException("Entry with playerId " + updateDTO.getPlayerId() + " does not exist");
        }

        LeaderboardEntry updatedEntry = new LeaderboardEntry();
        updatedEntry.setId(entry.get().getId());
        updatedEntry.setPlayerId(updateDTO.getPlayerId());
        updatedEntry.setElo(updateDTO.getElo());
        //updatedEntry.setRanking(updateDTO.getRanking());
        updatedEntry.setLastUpdated(LocalDateTime.now());

        leaderboardRepository.save(updatedEntry);

        return DTOUtil.convertEntryToDTO(updatedEntry);
    }

    


    
}
