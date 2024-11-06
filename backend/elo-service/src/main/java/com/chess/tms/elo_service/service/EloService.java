package com.chess.tms.elo_service.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.chess.tms.elo_service.model.EloHistory;
import com.chess.tms.elo_service.repository.EloRepository;
import com.chess.tms.elo_service.enums.Reason;

import java.time.LocalDateTime;

import jakarta.transaction.Transactional;

import com.chess.tms.elo_service.exception.InvalidReasonException;
import com.chess.tms.elo_service.exception.PlayerHistoryNotFoundException;
import com.chess.tms.elo_service.dto.DTOUtil;
import com.chess.tms.elo_service.dto.EloHistoryChartDTO;
import com.chess.tms.elo_service.dto.EloHistoryRequestDTO;
import com.chess.tms.elo_service.dto.EloRequestDTO;
import com.chess.tms.elo_service.dto.EloResponseDTO;
import com.chess.tms.elo_service.dto.MatchEloRequestDTO;
import com.chess.tms.elo_service.dto.WinLossUpdateDTO;

@Service
public class EloService {

    @Value("${leaderboard.service.url}")
    private String leaderboardServiceUrl;

    @Value("${players.service.url}")
    private String playersServiceUrl;

    @Autowired
    private EloRepository eloRepository;
    
    @Autowired
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

    public EloResponseDTO saveEloHistory(int oldElo, EloHistoryRequestDTO dto) {
        EloHistory newEloHistory = new EloHistory();

        newEloHistory.setOldElo(oldElo);
        newEloHistory.setNewElo(dto.getNewElo());
        newEloHistory.setPlayerId(dto.getPlayerId());
        newEloHistory.setChangeReason(dto.getChangeReason());
        newEloHistory.setCreatedAt(LocalDateTime.now());
        System.out.println("Running " + newEloHistory);
        eloRepository.save(newEloHistory);
        return DTOUtil.convertEntryToResponseDTO(newEloHistory);
    }
    
    //for testing
    // public EloResponseDTO saveEloHistory(EloHistoryRequestDTO dto) {
    //     EloHistory newEloHistory = new EloHistory();
    //     List<EloHistory> list = eloRepository.findByPlayerIdOrderByCreatedAtDesc(dto.getPlayerId());
    //     if (!list.isEmpty()) {
    //         newEloHistory.setOldElo(list.get(0).getNewElo());
    //     }
    //     newEloHistory.setNewElo(dto.getNewElo());
    //     newEloHistory.setPlayerId(dto.getPlayerId());
    //     newEloHistory.setChangeReason(dto.getChangeReason());
    //     newEloHistory.setCreatedAt(LocalDateTime.now());

    //     eloRepository.save(newEloHistory);

    //     return DTOUtil.convertEntryToResponseDTO(newEloHistory);
    // }

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

    public int[] calculateEloChange(int winnerElo, int loserElo) {
        double winnerWinProb = 1.0 / (1.0 + (Math.pow(10, (loserElo - winnerElo) / 400)));
        double loserWinProb = 1.0 / (1.0 + (Math.pow(10, (winnerElo - loserElo) / 400)));

        // System.out.println("p1: " + winnerWinProb);
        // System.out.println("p2: " + loserWinProb);

        int k = 32;

        int[] changedElo = new int[2];

        changedElo[0] = (int) (winnerElo + (1.0 - winnerWinProb) * k);
        changedElo[1] = (int) (loserElo + (0.0 - loserWinProb) * k);

        if (changedElo[0] == winnerElo) changedElo[0] += 1;
        if (changedElo[1] == loserElo) changedElo[1] -= 1;

        return changedElo;
    }

    public String updateMatchPlayersElo(MatchEloRequestDTO dto) {
        System.out.println("Running updateMatchPlayersElo in Elo Service 2");
        long winnerId = dto.getWinner();
        long loserId = dto.getLoser();

        // Get Elo of Players
        System.out.println("Running get for player elo 1");
        int winnerElo = restTemplate.getForObject(playersServiceUrl + "/api/player/elo/" + winnerId,
                        Integer.class);
        int loserElo = restTemplate.getForObject(playersServiceUrl + "/api/player/elo/" + loserId,
                            Integer.class);
        System.out.println("Running get for player elo 2");
        //testing
        // int winnerElo = eloRepository.findByPlayerIdOrderByCreatedAtDesc(winnerId).get(0).getNewElo();
        // int loserElo = eloRepository.findByPlayerIdOrderByCreatedAtDesc(loserId).get(0).getNewElo();

        EloRequestDTO winner = new EloRequestDTO(winnerId, winnerElo);
        EloRequestDTO loser = new EloRequestDTO(loserId, loserElo);

        System.out.println("winnerelo" + winnerElo);
        System.out.println("loserelo" + loserElo);

        // Elo Algorithm
        int[] changedElo = calculateEloChange(winner.getCurrentElo(), loser.getCurrentElo());
        int newWinnerElo = changedElo[0];
        int newLoserElo = changedElo[1];

        System.out.println("new winnerelo" + newWinnerElo);
        System.out.println("new loserelo" + newLoserElo);


        // Save Elo History
        EloHistoryRequestDTO winnerHistory = new EloHistoryRequestDTO(winner.getPlayerId(), newWinnerElo, Reason.WIN);
        EloHistoryRequestDTO loserHistory = new EloHistoryRequestDTO(loser.getPlayerId(), newLoserElo, Reason.LOSS);
        System.out.println("Running saveEloHistory(newLoserElo, loserHistory)");
        saveEloHistory(winnerElo, winnerHistory); // issue 
        saveEloHistory(loserElo, loserHistory);

        // Update Player's Elo
        String updateServiceUrl = playersServiceUrl + "/api/player/updateWinLossElo" ;
    
        WinLossUpdateDTO winDto = new WinLossUpdateDTO(winnerId, newWinnerElo, true);
        WinLossUpdateDTO lossDto = new WinLossUpdateDTO(loserId, newLoserElo, false);
        System.out.println("Running put for updateWinLossElo");
        restTemplate.put(updateServiceUrl, winDto);
        restTemplate.put(updateServiceUrl, lossDto);

        System.out.println("Players' elo updated successfully");
        return "Players' elo updated successfully";
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