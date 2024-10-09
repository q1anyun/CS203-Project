package com.chess.tms.elo_service.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.chess.tms.elo_service.dto.DTOUtil;
import com.chess.tms.elo_service.dto.EloHistoryRequestDTO;
import com.chess.tms.elo_service.model.EloHistory;
import com.chess.tms.elo_service.repository.EloRepository;
import com.chess.tms.elo_service.dto.EloUpdateDTO;
import com.chess.tms.elo_service.dto.MatchEloRequestDTO;
import com.chess.tms.elo_service.dto.EloRequestDTO;
import com.chess.tms.elo_service.dto.EloResponseDTO;
import com.chess.tms.elo_service.enums.Reason;

import java.time.LocalDateTime;

import jakarta.transaction.Transactional;

import com.chess.tms.elo_service.exception.InvalidReasonException;
import com.chess.tms.elo_service.exception.PlayerHistoryNotFoundException;

@Service
public class EloService {

    @Value("${leaderboard.service.url}")
    private String leaderboardServiceUrl;

    @Value("${players.service.url}")
    private String playersServiceUrl;
    
    private EloRepository eloRepository;

    private RestTemplate restTemplate;

    public EloService(EloRepository eloRepository, RestTemplate restTemplate) {
        this.eloRepository = eloRepository;
        this.restTemplate = restTemplate;
    }

    public List<EloResponseDTO> findAllByEloHistory() {
        List<EloHistory> list = eloRepository.findAll();
        List<EloResponseDTO> dtoList = DTOUtil.convertEntriesToResponseDTOs(list);

        return dtoList;
    }

    public List<EloResponseDTO> findEloHistoryByPlayerId(long playerId) {
        Optional<List<EloHistory>> list = eloRepository.findByPlayerId(playerId);
        if (list.isEmpty()) {
            throw new PlayerHistoryNotFoundException("Player with player id " + playerId + " has no history.");
        }

        List<EloResponseDTO> dtoList = DTOUtil.convertEntriesToResponseDTOs(list.get());
        return dtoList;
    }

    @Transactional
    public List<EloResponseDTO> deleteByPlayerId(long playerId) {
        Optional<List<EloHistory>> list = eloRepository.findByPlayerId(playerId);
        if (list.isEmpty()) {
            throw new PlayerHistoryNotFoundException("Player with player id " + playerId + " has no history");
        }
        List<EloResponseDTO> deleted = DTOUtil.convertEntriesToResponseDTOs(list.get());
        
        eloRepository.deleteByPlayerId(playerId);
        return deleted;
    }

    public EloResponseDTO saveEloHistory(EloHistoryRequestDTO dto) {
        List<EloHistory> list = eloRepository.findByPlayerIdOrderByCreatedAtDesc(dto.getPlayerId());
        EloHistory newEloHistory = new EloHistory();

        newEloHistory.setOldElo(dto.getNewElo());
        if (!list.isEmpty()) {
            newEloHistory.setOldElo(list.get(0).getNewElo());
        }
        newEloHistory.setNewElo(dto.getNewElo());
        newEloHistory.setPlayerId(dto.getPlayerId());
        newEloHistory.setChangeReason(dto.getChangeReason());
        newEloHistory.setCreatedAt(LocalDateTime.now());
       
        eloRepository.save(newEloHistory);
        //System.out.println(leaderboardServiceUrl + "/api/leaderboard/updateElo");
        //restTemplate.put(leaderboardServiceUrl + "/api/leaderboard/updateElo",  new EloUpdateDTO(dto.getPlayerId(), dto.getNewElo()));
        
        return DTOUtil.convertEntryToResponseDTO(newEloHistory);
    }

    public List<EloResponseDTO> findByPlayerIdAndChangeReason(long playerId, String changeReason) {
        Reason reason = Reason.WIN;  
        if (changeReason.equals("win")) {
            reason = Reason.WIN;
        } else if (changeReason.equals("loss")) {
            reason = Reason.LOSS;
        } else {
            throw new InvalidReasonException("Reason: " + changeReason + " is not valid. Valid reasons: win, loss, draw"); 
        }
        
        List<EloHistory> list = eloRepository.findByPlayerIdAndChangeReasonOrderByCreatedAtDesc(playerId, reason);
        List<EloResponseDTO> responses = DTOUtil.convertEntriesToResponseDTOs(list);
        return responses;
    }

    public void updateMatchPlayersElo(MatchEloRequestDTO dto){
        EloRequestDTO winner = dto.getWinner();
        EloRequestDTO loser = dto.getLoser();

        // Elo Algorithm
        int newWinnerElo = winner.getCurrentElo() + 1;
        int newLoserElo = loser.getCurrentElo() - 1;

        // Update Player's Elo
        String winnerServiceUrl = playersServiceUrl + "/api/player/elo/" + winner.getPlayerId();
        String loserServiceUrl = playersServiceUrl + "/api/player/elo/" + loser.getPlayerId();

        restTemplate.put(winnerServiceUrl, newWinnerElo);
        restTemplate.put(loserServiceUrl,  newLoserElo);

        // Save Elo History
        EloHistoryRequestDTO winnerHistory = new EloHistoryRequestDTO(winner.getPlayerId(), newWinnerElo, Reason.WIN);
        EloHistoryRequestDTO loserHistory = new EloHistoryRequestDTO(loser.getPlayerId(), newLoserElo, Reason.LOSS);
        saveEloHistory(winnerHistory);
        saveEloHistory(loserHistory);
    }
}