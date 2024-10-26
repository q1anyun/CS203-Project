package com.chess.tms.match_service.service;

import com.chess.tms.match_service.dto.MatchDTO;
import com.chess.tms.match_service.dto.MatchEloRequestDTO;
import com.chess.tms.match_service.dto.PlayerDetailsDTO;
import com.chess.tms.match_service.dto.TournamentDTO;
import com.chess.tms.match_service.dto.TournamentPlayerEloDTO;
import com.chess.tms.match_service.exception.GameTypeNotFoundException;
import com.chess.tms.match_service.exception.MatchDoesNotExistException;
import com.chess.tms.match_service.exception.PlayerDoesNotExistInMatchException;
import com.chess.tms.match_service.exception.MatchAlreadyCompletedException;
import com.chess.tms.match_service.exception.RoundTypeNotFoundException;
import com.chess.tms.match_service.exception.SwissBracketNotFoundException;
import com.chess.tms.match_service.model.*;
import com.chess.tms.match_service.repository.GameTypeRepository;
import com.chess.tms.match_service.repository.MatchesRepository;
import com.chess.tms.match_service.repository.RoundTypeRepository;
import com.chess.tms.match_service.repository.SwissBracketRepository;
import com.chess.tms.match_service.repository.SwissStandingRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MatchService {

    @Autowired
    private MatchesRepository matchRepository;

    @Autowired
    private RoundTypeRepository roundTypeRepository;

    @Autowired
    private GameTypeRepository gameTypeRepository;

    @Autowired
    private SwissBracketRepository swissBracketRepository;

    @Autowired
    private SwissStandingRepository swissStandingRepository;

    private final RestTemplate restTemplate;

    @Value("${tournaments.service.url}")
    private String tournamentServiceUrl;

    @Value("${elo.service.url}")
    private String eloServiceUrl;

    @Value("${players.service.url}")
    private String playerServiceUrl;

    public MatchService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Create initial matches for a tournament based on the number of players
    // Players are paired based on their Elo ratings in descending order - Higher
    // Elo players are paired with lower Elo players
    // Example: 8 players -> 4 first round matches -> 2 semi-final matches -> 1
    // final match
    // Example (1 Bye): 9 players -> 7 first round matches -> 4 quarter-final
    // matches -> 2 semi-final matches -> 1 final match
    // Example (2 Bye): 10 players -> 6 first round matches -> 3 quarter-final
    // matches -> 2 semi-final matches -> 1 final match
    public Long createKnockoutMatches(Long tournamentId, Long gameTypeId, List<Long> advancedPlayerIds) {
        TournamentPlayerEloDTO[] players;
        if (advancedPlayerIds == null) {
                    // Get the list of players with Elo ratings from the tournament service
        ResponseEntity<TournamentPlayerEloDTO[]> response = restTemplate.exchange(
            tournamentServiceUrl + "/api/tournament-players/" + tournamentId, HttpMethod.GET, null,
            TournamentPlayerEloDTO[].class);
            players = response.getBody();
        } else{
            players = advancedPlayerIds.stream()
                    .map(playerId -> restTemplate.getForObject(playerServiceUrl + "/api/player/" + playerId, PlayerDetailsDTO.class))
                    .map(player -> new TournamentPlayerEloDTO(player.getId(), player.getEloRating()))
                    .toArray(TournamentPlayerEloDTO[]::new);
        }
        int totalPlayers = players.length;

        // Calculate the next power of 2 for the knockout stage
        int nextPowerOfTwo = (int) Math.pow(2, Math.ceil(Math.log(totalPlayers) / Math.log(2)));
        int byes = nextPowerOfTwo - totalPlayers;

        RoundType currentRoundType = getRoundType(nextPowerOfTwo);

        // Sort players by their Elo rating (descending order for best players first)
        Arrays.sort(players, Comparator.comparingInt(TournamentPlayerEloDTO::getEloRating).reversed());

        // Prepare the matches for all rounds (create all rounds in advance)
        List<Match> allMatches = new ArrayList<>();
        Map<Integer, List<Match>> roundMatchesMap = new HashMap<>();

        // Handle byes for top players first
        System.out.println("Handle Byes");
        List<Match> byeMatches = new ArrayList<>();
        for (int i = 0; i < byes; i++) {
            TournamentPlayerEloDTO playerWithBye = players[i]; // Top player gets a bye
            Match byeMatch = new Match();
            byeMatch.setTournamentId(tournamentId);
            byeMatch.setPlayer1Id(null);
            byeMatch.setPlayer2Id(null);
            byeMatch.setWinnerId(playerWithBye.getId());
            byeMatch.setRoundType(currentRoundType);
            byeMatch.setGameType(gameTypeRepository.findById(gameTypeId)
                    .orElseThrow(() -> new GameTypeNotFoundException("GameType with ID " + gameTypeId + " not found")));
            byeMatch.setStatus(Match.MatchStatus.COMPLETED);
            byeMatches.add(byeMatch);
            allMatches.add(byeMatch);

            System.out.println("Player with bye: " + playerWithBye.getId());
            System.out.println("Player with bye: " + playerWithBye.getEloRating());
        }
        System.out.println();

        // Create first round matches with the remaining players after the byes
        List<Match> firstRoundMatches = new ArrayList<>();
        int lowIndex = byes; // Start pairing from the players after the byes
        int highIndex = totalPlayers - 1;

        System.out.println("Generate First Round Matches");
        // Pair the remaining players based on highest vs lowest elo
        while (lowIndex < highIndex) {
            TournamentPlayerEloDTO player1 = players[lowIndex]; // Higher Elo
            TournamentPlayerEloDTO player2 = players[highIndex]; // Lower Elo

            Match match = new Match();
            match.setTournamentId(tournamentId);
            match.setPlayer1Id(player1.getId());
            match.setPlayer2Id(player2.getId());
            match.setRoundType(currentRoundType);
            match.setGameType(gameTypeRepository.findById(gameTypeId)
                    .orElseThrow(() -> new GameTypeNotFoundException("GameType with ID " + gameTypeId + " not found")));
            match.setStatus(Match.MatchStatus.PENDING);
            firstRoundMatches.add(match);
            allMatches.add(match);

            lowIndex++;
            highIndex--;
            System.out.println("Player 1: " + player1.getId() + " vs Player 2: " + player2.getId());
            System.out.println("Player 1: " + player1.getEloRating() + " vs Player 2: " + player2.getEloRating());
        }

        // Combine first-round matches and bye matches
        List<Match> combinedFirstRoundMatches = new ArrayList<>(firstRoundMatches);
        combinedFirstRoundMatches.addAll(byeMatches);
        roundMatchesMap.put(1, combinedFirstRoundMatches);

        // Generate matches for subsequent rounds
        int currentRoundSize = (firstRoundMatches.size() + byeMatches.size()) / 2;
        int roundNumber = 2;
        System.out.println();
        System.out.println("Generate Subsequent Rounds");
        while (currentRoundSize >= 1) {
            List<Match> currentRoundMatches = new ArrayList<>();
            for (int i = 0; i < currentRoundSize; i++) {
                System.out.println("Round " + roundNumber + " Match " + i);
                Match nextRoundMatch = new Match();
                nextRoundMatch.setTournamentId(tournamentId);
                nextRoundMatch.setRoundType(getRoundType(currentRoundSize * 2));
                nextRoundMatch.setGameType(gameTypeRepository.findById(gameTypeId)
                        .orElseThrow(
                                () -> new GameTypeNotFoundException("GameType with ID " + gameTypeId + " not found")));
                nextRoundMatch.setStatus(Match.MatchStatus.PENDING);
                currentRoundMatches.add(nextRoundMatch);
                allMatches.add(nextRoundMatch);
            }
            roundMatchesMap.put(roundNumber, currentRoundMatches);
            currentRoundSize /= 2;
            roundNumber++;
        }

        // Save all the matches (both regular and bye matches)
        matchRepository.saveAll(allMatches);

        // Update next_match_id for each match in subsequent rounds
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

        return currentRoundType.getId();
    }

    /**
     * Create matches for a tournament based on the Swiss tournament format.
     * In a Swiss tournament, players are paired with other players with similar
     * standings (based on wins).
     * Players accumulate points as they progress through the rounds without being
     * eliminated.
     *
     * @param tournamentId the ID of the tournament
     * @param gameTypeId   the ID of the game type
     */
    public void createSwissMatches(Long tournamentId, Long gameTypeId) {
        // Step 1: Fetch all players and initialize standings
        ResponseEntity<TournamentPlayerEloDTO[]> response = restTemplate.exchange(
                tournamentServiceUrl + "/api/tournament-players/" + tournamentId, HttpMethod.GET, null,
                TournamentPlayerEloDTO[].class);

        TournamentPlayerEloDTO[] players = response.getBody();
        int totalPlayers = players.length;

        // Step 2: Create Swiss bracket entity and initialize standings
        SwissBracket bracket = new SwissBracket();
        bracket.setTournamentId(tournamentId);
        bracket.setCurrentRound(1);
        bracket.setNumberOfRounds((int) Math.ceil(Math.log(totalPlayers) / Math.log(2)) + 1); // Set number of rounds
        swissBracketRepository.save(bracket);

        // Initialize each player’s Swiss standing
        for (TournamentPlayerEloDTO player : players) {
            SwissStanding standing = new SwissStanding();
            standing.setBracketId(bracket.getId());
            standing.setTournamentPlayerId(player.getId());
            standing.setPlayerId(player.getId());
            standing.setWins(0);
            standing.setLosses(0);
            swissStandingRepository.save(standing);
        }

        // Step 4: Generate matches for the first round
        createRoundMatches(players, tournamentId, gameTypeId, bracket.getCurrentRound());
    }

    // Create matches between two groups, ensuring no players are left unpaired
    private void createRoundMatches(TournamentPlayerEloDTO[] players, Long tournamentId, Long gameTypeId,
            int roundNumber) {
        List<Match> matches = new ArrayList<>();
        Random random = new Random();

        // Fetch previous opponents to avoid repeated pairings
        Map<Long, Set<Long>> playerMatchHistory = getPreviousOpponents(tournamentId);

        // Sort players by Elo rating in descending order
        Arrays.sort(players, Comparator.comparingInt(TournamentPlayerEloDTO::getEloRating).reversed());

        // Split the players into two groups
        int midIndex = players.length / 2;
        List<TournamentPlayerEloDTO> group1 = new ArrayList<>(Arrays.asList(players).subList(0, midIndex));
        List<TournamentPlayerEloDTO> group2 = new ArrayList<>(Arrays.asList(players).subList(midIndex, players.length));

        // Shuffle both groups to add randomness in pairings
        Collections.shuffle(group1, random);
        Collections.shuffle(group2, random);

        // Pair players between the two groups
        boolean[] pairedGroup2 = new boolean[group2.size()]; // Track paired players in group2

        System.out.println("Group 1: " + group1.toString());
        System.out.println("Group 2: " + group2.toString());

        for (int i = 0; i < group1.size(); i++) {
            TournamentPlayerEloDTO player1 = group1.get(i);

            // Look for a suitable opponent in group2
            TournamentPlayerEloDTO player2 = null;
            for (int j = 0; j < group2.size(); j++) {
                if (!pairedGroup2[j] && !playerMatchHistory.getOrDefault(player1.getId(), new HashSet<>())
                        .contains(group2.get(j).getId())) {
                    player2 = group2.get(j);
                    pairedGroup2[j] = true; // Mark player in group2 as paired
                    break;
                }
            }

            // If no opponent is found, log or handle the issue appropriately
            if (player2 == null) {
                throw new IllegalStateException("Could not find a valid opponent for player " + player1.getId());
            }

            // Create and save the match
            createAndAddMatch(matches, player1, player2, tournamentId, gameTypeId, roundNumber);

            // Update player match history
            playerMatchHistory.putIfAbsent(player1.getId(), new HashSet<>());
            playerMatchHistory.putIfAbsent(player2.getId(), new HashSet<>());
            playerMatchHistory.get(player1.getId()).add(player2.getId());
            playerMatchHistory.get(player2.getId()).add(player1.getId());
        }

        // Save all matches for this round
        matchRepository.saveAll(matches);
    }

    // Helper method to create and add a match to the list
    private void createAndAddMatch(List<Match> matches, TournamentPlayerEloDTO player1,
            TournamentPlayerEloDTO player2, Long tournamentId, Long gameTypeId, Integer roundNumber) {
        Match match = new Match();
        match.setTournamentId(tournamentId);
        match.setPlayer1Id(player1.getId());
        match.setPlayer2Id(player2.getId());
        match.setSwissRoundNumber(roundNumber);
        match.setRoundType(getRoundTypeForSwissRound()); // Define this to get the round type for Swiss rounds

        // Fetch the GameType entity from the repository and set it
        GameType gameType = gameTypeRepository.findById(gameTypeId)
                .orElseThrow(() -> new GameTypeNotFoundException("GameType with ID " + gameTypeId + " not found"));
        match.setGameType(gameType);

        match.setStatus(Match.MatchStatus.PENDING);

        // Add the match to the matches list
        matches.add(match);
    }

    // Fetch previous opponents from the repository
    private Map<Long, Set<Long>> getPreviousOpponents(Long tournamentId) {
        // Find all matches for the specified tournament
        List<Match> previousMatches = matchRepository.findByTournamentId(tournamentId);

        // Create a map to hold the match history of each player
        Map<Long, Set<Long>> playerMatchHistory = new HashMap<>();

        for (Match match : previousMatches) {
            Long player1Id = match.getPlayer1Id();
            Long player2Id = match.getPlayer2Id();

            // Ensure both player IDs are present in the map
            playerMatchHistory.putIfAbsent(player1Id, new HashSet<>());
            playerMatchHistory.putIfAbsent(player2Id, new HashSet<>());

            // Record the match between the two players
            playerMatchHistory.get(player1Id).add(player2Id);
            playerMatchHistory.get(player2Id).add(player1Id);
        }

        return playerMatchHistory;
    }

    // Get the RoundType based on the number of players
    private RoundType getRoundType(int numberOfPlayers) {
        return roundTypeRepository.findByNumberOfPlayers(numberOfPlayers)
                .orElseThrow(() -> new RoundTypeNotFoundException(
                        "Round type not found for " + numberOfPlayers + " players"));
    }

    // Get the RoundType for Swiss round
    private RoundType getRoundTypeForSwissRound() {
        return roundTypeRepository.findByRoundName("Swiss")
                .orElseThrow(() -> new RoundTypeNotFoundException("Round type not found for Swiss"));
    }

    private void updateCurrentRoundForTournament(Long tournamentId, Long roundTypeId) {
        String updateRoundUrl = tournamentServiceUrl + "/api/tournaments/" + tournamentId + "/round/" + roundTypeId;
        // Call the endpoint to update the current round
        restTemplate.put(updateRoundUrl, null);
    }

    public List<MatchDTO> getMatchesByTournament(Long tournamentId) {
        List<Match> matches = matchRepository.findByTournamentId(tournamentId);
        return matches.stream()
                .map(this::convertMatchToMatchDTO)
                .collect(Collectors.toList());
    }

    public MatchDTO getMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchDoesNotExistException("Match not found"));
        return convertMatchToMatchDTO(match);
    }

    public String advanceWinner(Long matchId, Long winnerId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchDoesNotExistException("Match not found"));

        if (match.getWinnerId() != null) {
            throw new MatchAlreadyCompletedException("Match has already been completed");
        }

        if (!match.getPlayer1Id().equals(winnerId) && !match.getPlayer2Id().equals(winnerId)) {
            throw new PlayerDoesNotExistInMatchException(
                    "Player with id " + winnerId + " is not recognised in the match.");
        }

        match.setWinnerId(winnerId);
        match.setLoserId(match.getPlayer1Id().equals(winnerId) ? match.getPlayer2Id() : match.getPlayer1Id());
        match.setStatus(Match.MatchStatus.COMPLETED);

        // Update Elo of players
        MatchEloRequestDTO matchEloRequestDTO = new MatchEloRequestDTO();
        matchEloRequestDTO.setWinner(match.getWinnerId());
        matchEloRequestDTO.setLoser(match.getLoserId());
        restTemplate.put(eloServiceUrl + "/api/elo/match", matchEloRequestDTO);

        matchRepository.save(match);

        if (match.getRoundType().getRoundName().equals("Swiss")) {
            return advanceSwissMatch(match, winnerId);
        } else {
            return advanceKnockoutMatch(match, winnerId);
        }
    }

    private String advanceSwissMatch(Match match, Long winnerId) {
        SwissBracket bracket = swissBracketRepository.findByTournamentId(match.getTournamentId())
                .orElseThrow(() -> new SwissBracketNotFoundException("Swiss bracket not found"));
    
        // Update the standings for the winner and loser
        SwissStanding winnerStanding = swissStandingRepository.findByBracketIdAndPlayerId(bracket.getId(), winnerId)
                .orElseThrow(() -> new PlayerDoesNotExistInMatchException("Player not found"));
    
        SwissStanding loserStanding = swissStandingRepository
                .findByBracketIdAndPlayerId(bracket.getId(), match.getLoserId())
                .orElseThrow(() -> new PlayerDoesNotExistInMatchException("Player not found"));
    
        winnerStanding.setWins(winnerStanding.getWins() + 1);
        loserStanding.setLosses(loserStanding.getLosses() + 1);
    
        swissStandingRepository.save(winnerStanding);
        swissStandingRepository.save(loserStanding);
    
        // Check if the round is completed
        List<Match> roundMatches = matchRepository.findByTournamentIdAndSwissRoundNumber(match.getTournamentId(),
                bracket.getCurrentRound());
    
        boolean roundCompleted = roundMatches.stream().allMatch(m -> m.getStatus() == Match.MatchStatus.COMPLETED);
    
        if (roundCompleted) {    
            // Check if the last Swiss round is reached
            if (bracket.getCurrentRound() == bracket.getNumberOfRounds()) {
                // Get the standings for the current bracket and sort by wins and losses for the top half
                List<SwissStanding> standings = swissStandingRepository.findByBracketIdOrderByWinsDescLossesAsc(bracket.getId());
                int half = standings.size() / 2;
    
                // Get the IDs of players who advanced
                List<Long> advancedPlayers = standings.subList(0, half)
                        .stream()
                        .map(SwissStanding::getPlayerId)
                        .collect(Collectors.toList());
    
                // Create knockout matches for the top half of players
                Long currentRoundId = createKnockoutMatches(match.getTournamentId(), match.getGameType().getId(), advancedPlayers);
                updateCurrentRoundForTournament(match.getTournamentId(), currentRoundId);
                return "Swiss rounds completed, moving to knockout phase.";
            } else {
                // Proceed to the next Swiss round
                // Update the current round for the bracket
                bracket.setCurrentRound(bracket.getCurrentRound() + 1);
                swissBracketRepository.save(bracket);
                // Fetch all players with their updated standings for the next round
                ResponseEntity<TournamentPlayerEloDTO[]> response = restTemplate.exchange(
                        tournamentServiceUrl + "/api/tournament-players/" + match.getTournamentId(),
                        HttpMethod.GET, null, TournamentPlayerEloDTO[].class);
    
                TournamentPlayerEloDTO[] allPlayers = response.getBody();
    
                // Create matches for the next round with all players
                createRoundMatches(allPlayers, match.getTournamentId(), match.getGameType().getId(),
                        bracket.getCurrentRound());
                return "Advanced to Swiss Round " + bracket.getCurrentRound();
            }
        }
        return "Swiss standings updated";
    }

    private String advanceKnockoutMatch(Match match, Long winnerId) {
        if (match.getNextMatchId() != null) {
            // Check if round is completed
            RoundType roundType = roundTypeRepository.findById(match.getRoundType().getId())
                    .orElseThrow(
                            () -> new RoundTypeNotFoundException("Round type not found while checking next round"));

            List<Match> roundMatches = matchRepository.findByTournamentIdAndRoundTypeId(match.getTournamentId(),
                    roundType.getId());

            boolean roundCompleted = roundMatches.stream().allMatch(m -> m.getStatus() == Match.MatchStatus.COMPLETED);

            if (roundCompleted) {
                Match nextMatch = matchRepository.findById(match.getNextMatchId())
                        .orElseThrow(() -> new MatchDoesNotExistException("Next match not found"));

                updateCurrentRoundForTournament(match.getTournamentId(), nextMatch.getRoundType().getId());
                // Assign matches for the next round
                for (Match completedMatch : roundMatches) {
                    Long nextMatchId = completedMatch.getNextMatchId();
                    if (nextMatchId != null) {
                        Match advanceMatch = matchRepository.findById(nextMatchId)
                                .orElseThrow(() -> new RuntimeException(
                                        "Next match not found for match ID: " + nextMatchId));
                        System.out.println(completedMatch.getWinnerId());
                        if (advanceMatch.getPlayer1Id() == null) {
                            advanceMatch.setPlayer1Id(completedMatch.getWinnerId());
                        } else if (advanceMatch.getPlayer2Id() == null) {
                            advanceMatch.setPlayer2Id(completedMatch.getWinnerId());
                        }
                        matchRepository.save(advanceMatch);
                    }
                }

                return "Tournament has advanced to the next round";
            }
            return "Winner advanced to the next round";
        } else {
            String updateTournamentUrl = tournamentServiceUrl + "/api/tournaments/" + match.getTournamentId()
                    + "/winner/" + winnerId;
            restTemplate.put(updateTournamentUrl, null);
            return "Tournament completed";
        }

    }

    public List<MatchDTO> getRecentMatchesByPlayerId(Long playerId) {

        List<Match> matches = matchRepository.findTop5ByPlayer1IdOrPlayer2IdOrderByUpdatedAtDesc(playerId, playerId);

        List<MatchDTO> matchDTOs = matches.stream()
                .map(this::convertMatchToMatchDTO)
                .collect(Collectors.toList());

        return matchDTOs;
    }

    private MatchDTO convertMatchToMatchDTO(Match match) {
        MatchDTO matchDTO = new MatchDTO();
        matchDTO.setId(match.getId());
        PlayerDetailsDTO player1 = new PlayerDetailsDTO();
        PlayerDetailsDTO player2 = new PlayerDetailsDTO();

        if (match.getPlayer1Id() != null) {
            player1 = getPlayerDetails(match.getPlayer1Id());
            matchDTO.setPlayer1(player1);
        } else {
            matchDTO.setPlayer1(null);
        }

        if (match.getPlayer2Id() != null) {
            player2 = getPlayerDetails(match.getPlayer2Id());
            matchDTO.setPlayer2(player2);
        } else {
            matchDTO.setPlayer2(null);
        }
        if (match.getWinnerId() != null) {
            matchDTO.setWinnerId(match.getWinnerId());
        } else {
            matchDTO.setWinnerId(null);
        }

        if (match.getLoserId() != null) {
            matchDTO.setLoserId(match.getLoserId());
        } else {
            matchDTO.setLoserId(null);
        }

        matchDTO.setSwissRoundNumber(match.getSwissRoundNumber());
        matchDTO.setRoundType(match.getRoundType());
        matchDTO.setGameType(match.getGameType());
        matchDTO.setDate(match.getUpdatedAt());

        TournamentDTO tournamentDTO = getTournamentDetails(match.getTournamentId());
        matchDTO.setTournament(tournamentDTO);

        return matchDTO;
    }

    private TournamentDTO getTournamentDetails(Long tournamentId) {
        String tournamentUrl = tournamentServiceUrl + "/api/tournaments/" + tournamentId;
        return restTemplate.getForObject(tournamentUrl, TournamentDTO.class);
    }

    private PlayerDetailsDTO getPlayerDetails(long playerId) {
        ResponseEntity<PlayerDetailsDTO> response = restTemplate.getForEntity(
                playerServiceUrl + "/api/player/" + playerId,
                PlayerDetailsDTO.class);

        return response.getBody();
    }
}
