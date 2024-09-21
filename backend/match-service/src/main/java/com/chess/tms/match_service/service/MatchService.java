package com.chess.tms.match_service.service;

import com.chess.tms.match_service.dto.TournamentPlayerDTO;
import com.chess.tms.match_service.exception.MatchDoesNotExistException;
import com.chess.tms.match_service.exception.PlayerDoesNotExistInMatchException;
import com.chess.tms.match_service.model.*;
import com.chess.tms.match_service.repository.GameTypeRepository;
import com.chess.tms.match_service.repository.MatchesRepository;
import com.chess.tms.match_service.repository.RoundTypeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class MatchService {

    @Autowired
    private MatchesRepository matchRepository;

    @Autowired
    private RoundTypeRepository roundTypeRepository;

    @Autowired
    private GameTypeRepository gameTypeRepository;

    private final RestTemplate restTemplate;

    @Value("${tournament.service.url}")
    private String tournamentServiceUrl;

    public MatchService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public TournamentPlayerDTO[] getTournamentPlayers() {
        TournamentPlayerDTO player1 = new TournamentPlayerDTO(1L, 1200);
        TournamentPlayerDTO player2 = new TournamentPlayerDTO(2L, 1300);
        TournamentPlayerDTO player3 = new TournamentPlayerDTO(3L, 1400);
        TournamentPlayerDTO player4 = new TournamentPlayerDTO(4L, 1250);
        TournamentPlayerDTO player5 = new TournamentPlayerDTO(5L, 1350);
        TournamentPlayerDTO player6 = new TournamentPlayerDTO(6L, 1150);
        TournamentPlayerDTO player7 = new TournamentPlayerDTO(7L, 1450);
        TournamentPlayerDTO player8 = new TournamentPlayerDTO(8L, 1100);
        TournamentPlayerDTO player9 = new TournamentPlayerDTO(9L, 1280);
        TournamentPlayerDTO player10 = new TournamentPlayerDTO(10L, 1380);
        TournamentPlayerDTO player11 = new TournamentPlayerDTO(11L, 1500);
        TournamentPlayerDTO player12 = new TournamentPlayerDTO(12L, 1250);
        TournamentPlayerDTO player13 = new TournamentPlayerDTO(13L, 1320);
        TournamentPlayerDTO player14 = new TournamentPlayerDTO(14L, 1480);
        TournamentPlayerDTO player15 = new TournamentPlayerDTO(15L, 1150);
        TournamentPlayerDTO player16 = new TournamentPlayerDTO(16L, 1420);
        TournamentPlayerDTO player17 = new TournamentPlayerDTO(17L, 1350);
        TournamentPlayerDTO player18 = new TournamentPlayerDTO(18L, 1290);

        return new TournamentPlayerDTO[] {
                player1, player2, player3, player4, player5, player6, player7, player8, player9,
                player10, player11, player12, player13, player14, player15, player16, player17, player18
        };
    }

    public void createInitialMatches(Long tournamentId, Long gameTypeId) {
        TournamentPlayerDTO[] players = getTournamentPlayers();
        int totalPlayers = players.length;

        // Calculate the next power of 2 for the knockout stage
        int nextPowerOfTwo = (int) Math.pow(2, Math.ceil(Math.log(totalPlayers) / Math.log(2)));
        int byes = nextPowerOfTwo - totalPlayers;

        // Sort players by their Elo rating (descending order for best players first)
        Arrays.sort(players, Comparator.comparingInt(TournamentPlayerDTO::getEloRating).reversed());

        // Prepare the matches for all rounds (create all rounds in advance)
        List<Match> allMatches = new ArrayList<>();
        Map<Integer, List<Match>> roundMatchesMap = new HashMap<>();

        // Create first round matches
        List<Match> firstRoundMatches = new ArrayList<>();
        for (int i = 0; i < totalPlayers - byes; i += 2) {
            TournamentPlayerDTO player1 = players[i];
            TournamentPlayerDTO player2 = players[i + 1];

            Match match = new Match();
            match.setTournamentId(tournamentId);
            match.setPlayer1Id(player1.getId());
            match.setPlayer2Id(player2.getId());
            match.setRoundType(getRoundType(nextPowerOfTwo));
            match.setGameTypeId(gameTypeId);
            match.setStatus(Match.MatchStatus.PENDING);
            firstRoundMatches.add(match);
            allMatches.add(match);
        }

        // Handle byes for top players
        List<Match> byeMatches = new ArrayList<>();
        for (int i = 0; i < byes; i++) {
            TournamentPlayerDTO playerWithBye = players[totalPlayers - 1 - i];
            Match byeMatch = new Match();
            byeMatch.setTournamentId(tournamentId);
            byeMatch.setPlayer1Id(null);
            byeMatch.setPlayer2Id(null);
            byeMatch.setWinnerId(playerWithBye.getId());
            byeMatch.setRoundType(getRoundType(nextPowerOfTwo));
            byeMatch.setGameTypeId(gameTypeId);
            byeMatch.setStatus(Match.MatchStatus.COMPLETED);
            byeMatches.add(byeMatch);
            allMatches.add(byeMatch);
        }

        List<Match> combinedFirstRoundMatches = new ArrayList<>();

        combinedFirstRoundMatches.addAll(firstRoundMatches);
        combinedFirstRoundMatches.addAll(byeMatches);

        roundMatchesMap.put(1, combinedFirstRoundMatches);

        // Generate the remaining rounds
        int currentRoundSize = (firstRoundMatches.size() + byeMatches.size()) / 2;
        int roundNumber = 2;

        while (currentRoundSize >= 1) {
            List<Match> currentRoundMatches = new ArrayList<>();
            for (int i = 0; i < currentRoundSize; i++) {
                Match nextRoundMatch = new Match();
                nextRoundMatch.setTournamentId(tournamentId);
                nextRoundMatch.setRoundType(getRoundType(currentRoundSize * 2));
                nextRoundMatch.setGameTypeId(gameTypeId);
                nextRoundMatch.setStatus(Match.MatchStatus.PENDING);
                currentRoundMatches.add(nextRoundMatch);
                allMatches.add(nextRoundMatch);
            }
            roundMatchesMap.put(roundNumber, currentRoundMatches);
            currentRoundSize /= 2;
            roundNumber++;
        }

        matchRepository.saveAll(allMatches);

        // Update next_match_id for each match
        for (int round = 1; round < roundNumber - 1; round++) {
            List<Match> previousRoundMatches = roundMatchesMap.get(round);
            List<Match> nextRoundMatches = roundMatchesMap.get(round + 1);

            for (int i = 0; i < previousRoundMatches.size(); i += 2) {
                Match previousMatch1 = previousRoundMatches.get(i);
                Match previousMatch2 = previousRoundMatches.get(i + 1);
                Match nextRoundMatch = nextRoundMatches.get(i / 2);

                previousMatch1.setNextMatchId(nextRoundMatch.getId());
                previousMatch2.setNextMatchId(nextRoundMatch.getId());
                matchRepository.save(previousMatch1);
                matchRepository.save(previousMatch2);
            }
        }
    }

    // Get the RoundType based on the number of players
    private RoundType getRoundType(int numberOfPlayers) {
        return roundTypeRepository.findByNumberOfPlayers(numberOfPlayers)
                .orElseThrow(() -> new RuntimeException("Round type not found for " + numberOfPlayers + " players"));
    }

    public List<Match> getMatchesByTournament(Long tournamentId) {
        return matchRepository.findByTournamentId(tournamentId);
    }

    public Match getMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchDoesNotExistException("Match not found"));

        return match;
    }

    // To do: Add check whether winnerId is part of the match
    public void advanceWinner(Long matchId, Long winnerId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new RuntimeException("Match not found"));

        if(match.getPlayer1Id() != winnerId || match.getPlayer2Id() != winnerId) {
            throw new PlayerDoesNotExistInMatchException("Player with id " + winnerId + " is not recognised in the match.");
        }

        match.setWinnerId(winnerId);
        match.setLoserId(match.getPlayer1Id().equals(winnerId) ? match.getPlayer2Id() : match.getPlayer1Id());
        match.setStatus(Match.MatchStatus.COMPLETED);

        matchRepository.save(match);

        if (match.getNextMatchId() != null) {
            Match nextMatch = matchRepository.findById(match.getNextMatchId())
                    .orElseThrow(() -> new RuntimeException("Next match not found"));

            if (nextMatch.getPlayer1Id() == null) {
                nextMatch.setPlayer1Id(winnerId);
            } else if (nextMatch.getPlayer2Id() == null) {
                nextMatch.setPlayer2Id(winnerId);
            }

            matchRepository.save(nextMatch);
        }
    }

    public void generateNextRound(Long tournamentId) {
        // Find the most recent completed round (the round with the minimum number of completed matches)
        // Might store current Round id in Tourmanent table instead of finding it every time
        RoundType mostRecentCompletedRound = roundTypeRepository.findMostRecentCompletedRound(tournamentId)
            .orElseThrow(() -> new RuntimeException("No completed rounds found for this tournament."));
    
        // Get the matches for that completed round
        List<Match> completedMatches = matchRepository.findCompletedMatchesByRoundType(tournamentId, mostRecentCompletedRound.getId());
    
        // Assign matches for the next round
        for (Match completedMatch : completedMatches) {
            Long nextMatchId = completedMatch.getNextMatchId();
            if (nextMatchId != null) {
                Match nextMatch = matchRepository.findById(nextMatchId)
                    .orElseThrow(() -> new RuntimeException("Next match not found for match ID: " + nextMatchId));
    
                System.out.println(nextMatchId);

                if (nextMatch.getPlayer1Id() == null) {
                    nextMatch.setPlayer1Id(completedMatch.getWinnerId());
                } else if (nextMatch.getPlayer2Id() == null) {
                    nextMatch.setPlayer2Id(completedMatch.getWinnerId());
                }

                matchRepository.save(nextMatch);
            }
        }
    }
}
