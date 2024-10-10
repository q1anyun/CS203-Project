package com.chess.tms.elo_service.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.chess.tms.elo_service.model.EloHistory;
import com.chess.tms.elo_service.repository.EloRepository;
import com.chess.tms.elo_service.enums.Reason;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.transaction.Transactional;

import com.chess.tms.elo_service.exception.InvalidReasonException;
import com.chess.tms.elo_service.exception.PlayerHistoryNotFoundException;
import com.chess.tms.elo_service.exception.dto.DTOUtil;
import com.chess.tms.elo_service.exception.dto.EloHistoryChartDTO;
import com.chess.tms.elo_service.exception.dto.EloHistoryRequestDTO;
import com.chess.tms.elo_service.exception.dto.EloRequestDTO;
import com.chess.tms.elo_service.exception.dto.EloResponseDTO;
import com.chess.tms.elo_service.exception.dto.EloUpdateDTO;
import com.chess.tms.elo_service.exception.dto.MatchEloRequestDTO;

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
        // System.out.println(leaderboardServiceUrl + "/api/leaderboard/updateElo");
        // restTemplate.put(leaderboardServiceUrl + "/api/leaderboard/updateElo", new
        // EloUpdateDTO(dto.getPlayerId(), dto.getNewElo()));

        return DTOUtil.convertEntryToResponseDTO(newEloHistory);
    }

    public List<EloResponseDTO> findByPlayerIdAndChangeReason(long playerId, String changeReason) {
        Reason reason = Reason.WIN;
        if (changeReason.equals("win")) {
            reason = Reason.WIN;
        } else if (changeReason.equals("loss")) {
            reason = Reason.LOSS;
        } else {
            throw new InvalidReasonException(
                    "Reason: " + changeReason + " is not valid. Valid reasons: win, loss, draw");
        }

        List<EloHistory> list = eloRepository.findByPlayerIdAndChangeReasonOrderByCreatedAtDesc(playerId, reason);
        List<EloResponseDTO> responses = DTOUtil.convertEntriesToResponseDTOs(list);
        return responses;
    }

    public int[] calculateEloChange(int elo1, int elo2) {
        double p1 = (1.0 / Math.pow(10, elo1 - elo2) / 400);
        double p2 = (1.0 / Math.pow(10, elo2 - elo1) / 400);
        int k = 40;

        int[] changedElo = new int[2];

        changedElo[0] = (int) (elo1 + (1.0 - p1) * k);
        changedElo[1] = (int) (elo2 + (1.0 - p2))
    }

    public void updateMatchPlayersElo(MatchEloRequestDTO dto) {
        EloRequestDTO winner = dto.getWinner();
        EloRequestDTO loser = dto.getLoser();

        // Elo Algorithm
        int newWinnerElo = winner.getCurrentElo() + 1;
        int newLoserElo = loser.getCurrentElo() - 1;

        // Update Player's Elo
        String winnerServiceUrl = playersServiceUrl + "/api/player/elo/" + winner.getPlayerId();
        String loserServiceUrl = playersServiceUrl + "/api/player/elo/" + loser.getPlayerId();

        restTemplate.put(winnerServiceUrl, newWinnerElo);
        restTemplate.put(loserServiceUrl, newLoserElo);

        // Save Elo History
        EloHistoryRequestDTO winnerHistory = new EloHistoryRequestDTO(winner.getPlayerId(), newWinnerElo, Reason.WIN);
        EloHistoryRequestDTO loserHistory = new EloHistoryRequestDTO(loser.getPlayerId(), newLoserElo, Reason.LOSS);
        saveEloHistory(winnerHistory);
        saveEloHistory(loserHistory);
    }

    public List<EloHistoryChartDTO> findPlayerEloHistoryForChart(long id) {
        List<EloHistory> eloHistoryList = eloRepository.findLatestEloHistoryByPlayerId(id);

        List<EloHistoryChartDTO> chartDTOs = eloHistoryList.stream()
                .map(history -> new EloHistoryChartDTO(history.getNewElo(), history.getCreatedAt().toLocalDate()))
                .sorted(Comparator.comparing(EloHistoryChartDTO::getDate))
                .collect(Collectors.toList());

        return chartDTOs;
    }
}