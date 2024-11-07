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
import com.chess.tms.match_service.model.Match.MatchStatus;
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

    @Autowired
    private RestTemplate restTemplate;

    @Value("${tournaments.service.url}")
    private String tournamentServiceUrl;

    @Value("${elo.service.url}")
    private String eloServiceUrl;

    @Value("${players.service.url}")
    private String playerServiceUrl;

    public MatchService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Creates knockout matches for a tournament based on the number of players.
     * Matches are paired based on Elo ratings with high-rated players facing
     * lower-rated players.
     *
     * @param tournamentId      the tournament ID
     * @param gameTypeId        the game type ID
     * @param advancedPlayerIds list of players advancing from previous rounds, or
     *                          null if initial round
     * @return ID of the initial round's RoundType
     */
    public Long createKnockoutMatches(Long tournamentId, Long gameTypeId, List<Long> advancedPlayerIds) {
        TournamentPlayerEloDTO[] players;
        if (advancedPlayerIds == null) {
            // Get the list of players with Elo ratings from the tournament service
            ResponseEntity<TournamentPlayerEloDTO[]> response = restTemplate.exchange(
                    tournamentServiceUrl + "/api/tournament-players/" + tournamentId, HttpMethod.GET, null,
                    TournamentPlayerEloDTO[].class);
            players = response.getBody();
        } else {
            players = advancedPlayerIds.stream()
                    .map(playerId -> restTemplate.getForObject(playerServiceUrl + "/api/player/" + playerId,
                            PlayerDetailsDTO.class))
                    .map(player -> new TournamentPlayerEloDTO(player.getId(), player.getEloRating()))
                    .toArray(TournamentPlayerEloDTO[]::new);
        }

        // Calculate next power of 2 for knockout structure and determine necessary byes
        int totalPlayers = players.length;
        int nextPowerOfTwo = (int) Math.pow(2, Math.ceil(Math.log(totalPlayers) / Math.log(2)));
        int byes = nextPowerOfTwo - totalPlayers;
        RoundType currentRoundType = getRoundType(nextPowerOfTwo);

        // Sort players by Elo rating (highest first)
        Arrays.sort(players, Comparator.comparingInt(TournamentPlayerEloDTO::getEloRating).reversed());

        // Prepare the matches for all rounds (create all rounds in advance)
        List<Match> allMatches = new ArrayList<>();
        Map<Integer, List<Match>> roundMatchesMap = new HashMap<>();

        // Handle byes for top players first
        List<Match> byeMatches = getByeMatches(tournamentId, gameTypeId, players, byes, currentRoundType, allMatches);

        // Create first round matches with the remaining players after the byes
        List<Match> firstRoundMatches = new ArrayList<>();
        int lowIndex = byes;
        int highIndex = totalPlayers - 1;
        generateFirstRoundMatches(tournamentId, gameTypeId, players, currentRoundType, allMatches, firstRoundMatches,
                lowIndex,
                highIndex);

        // Combine first-round and bye matches, add to map, and generate subsequent
        // rounds
        List<Match> combinedFirstRoundMatches = new ArrayList<>(firstRoundMatches);
        combinedFirstRoundMatches.addAll(byeMatches);
        roundMatchesMap.put(1, combinedFirstRoundMatches);

        int currentRoundSize = (firstRoundMatches.size() + byeMatches.size()) / 2;
        int roundNumber = 2;
        roundNumber = generateSubsequentMatches(tournamentId, gameTypeId, allMatches, roundMatchesMap, currentRoundSize,
                roundNumber);

        // Save all the matches (both regular and bye matches) and update next_match_id
        // for each match in subsequent rounds
        matchRepository.saveAll(allMatches);
        updateNextMatchId(roundMatchesMap, roundNumber);

        return currentRoundType.getId();
    }

    /**
     * Generates the first round of knockout matches by pairing players based on Elo
     * ratings.
     */
    private void generateFirstRoundMatches(Long tournamentId, Long gameTypeId, TournamentPlayerEloDTO[] players,
            RoundType currentRoundType, List<Match> allMatches, List<Match> firstRoundMatches, int lowIndex,
            int highIndex) {

        while (lowIndex < highIndex) {
            TournamentPlayerEloDTO player1 = players[lowIndex];
            TournamentPlayerEloDTO player2 = players[highIndex];

            Match match = createMatch(tournamentId, player1.getId(), player2.getId(), null, null, null,
                    currentRoundType, gameTypeId, null, Match.MatchStatus.PENDING);

            firstRoundMatches.add(match);
            allMatches.add(match);

            lowIndex++;
            highIndex--;

        }
    }

    /**
     * Generates subsequent rounds by creating matches for each successive round
     * until the final match.
     */
    private int generateSubsequentMatches(Long tournamentId, Long gameTypeId, List<Match> allMatches,
            Map<Integer, List<Match>> roundMatchesMap, int currentRoundSize, int roundNumber) {
        while (currentRoundSize >= 1) {
            List<Match> currentRoundMatches = new ArrayList<>();
            for (int i = 0; i < currentRoundSize; i++) {
                System.out.println("Round " + roundNumber + " Match " + i);

                RoundType roundType = getRoundType(currentRoundSize * 2);
                Match nextRoundMatch = createMatch(tournamentId, null, null, null, null, null, roundType, gameTypeId,
                        null, Match.MatchStatus.PENDING);
                currentRoundMatches.add(nextRoundMatch);
                allMatches.add(nextRoundMatch);
            }
            roundMatchesMap.put(roundNumber, currentRoundMatches);
            currentRoundSize /= 2;
            roundNumber++;
        }
        return roundNumber;
    }

    /**
     * Updates next_match_id for each match in subsequent rounds to maintain
     * knockout structure.
     */
    private void updateNextMatchId(Map<Integer, List<Match>> roundMatchesMap, int roundNumber) {
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

    /**
     * Creates and returns matches for players receiving a bye in the first round.
     */
    private List<Match> getByeMatches(Long tournamentId, Long gameTypeId, TournamentPlayerEloDTO[] players, int byes,
            RoundType currentRoundType, List<Match> allMatches) {
        System.out.println("Handle Byes");
        List<Match> byeMatches = new ArrayList<>();
        for (int i = 0; i < byes; i++) {
            TournamentPlayerEloDTO playerWithBye = players[i]; // Top player gets a bye
            Match byeMatch = createMatch(tournamentId, null, null, playerWithBye.getId(), null, null, currentRoundType,
                    gameTypeId, null, Match.MatchStatus.COMPLETED);
            byeMatches.add(byeMatch);
            allMatches.add(byeMatch);

            System.out.println("Player with bye: " + playerWithBye.getId());
            System.out.println("Player with bye: " + playerWithBye.getEloRating());
        }
        System.out.println();
        return byeMatches;
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
    public Long createSwissMatches(Long tournamentId, Long gameTypeId) {
        ResponseEntity<TournamentPlayerEloDTO[]> response = restTemplate.exchange(
                tournamentServiceUrl + "/api/tournament-players/" + tournamentId, HttpMethod.GET, null,
                TournamentPlayerEloDTO[].class);

        TournamentPlayerEloDTO[] players = response.getBody();
        int totalPlayers = players.length;

        SwissBracket bracket = getSwissBracket(tournamentId, totalPlayers);
        setSwissStanding(players, bracket);

        createRoundMatches(players, tournamentId, gameTypeId, bracket.getCurrentRound());
        System.out.println("Returning ID: " + bracket.getId());
        return bracket.getId();
    }

    /**
     * Initializes standings for each player in a Swiss bracket.
     *
     * @param players array of players in the tournament
     * @param bracket the Swiss bracket for the tournament
     */
    private void setSwissStanding(TournamentPlayerEloDTO[] players, SwissBracket bracket) {

        for (TournamentPlayerEloDTO player : players) {
            SwissStanding standing = new SwissStanding();
            standing.setBracket(bracket);
            standing.setTournamentPlayerId(player.getId());
            standing.setPlayerId(player.getId());
            standing.setWins(0);
            standing.setLosses(0);
            swissStandingRepository.save(standing);
        }
    }

    /**
     * Creates and saves a SwissBracket with the appropriate number of rounds based
     * on total players.
     *
     * @param tournamentId the ID of the tournament
     * @param totalPlayers the number of players in the tournament
     * @return the created SwissBracket
     */
    private SwissBracket getSwissBracket(Long tournamentId, int totalPlayers) {
        SwissBracket bracket = new SwissBracket();
        bracket.setTournamentId(tournamentId);
        bracket.setCurrentRound(1);
        bracket.setNumberOfRounds((int) Math.ceil(Math.log(totalPlayers) / Math.log(2)) + 1); // Set number of rounds
        swissBracketRepository.save(bracket);
        return bracket;
    }

    /**
     * Creates matches between players for a Swiss round, ensuring that players with
     * similar standings are paired.
     * Players from each group (split based on Elo) are paired in a balanced way to
     * ensure fair competition.
     *
     * @param players      array of players participating in the round
     * @param tournamentId the tournament ID
     * @param gameTypeId   the game type ID
     * @param roundNumber  the current round number in the Swiss bracket
     */

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
            player2 = findSuitableOpponent(playerMatchHistory, group2, pairedGroup2, player1, player2);

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

    /**
     * Finds a suitable opponent in group2 for a player from group1, ensuring no
     * prior match history between them.
     *
     * @param playerMatchHistory map containing each player's past opponents
     * @param group2             the group of potential opponents
     * @param pairedGroup2       boolean array tracking if players in group2 are
     *                           already paired
     * @param player1            the player looking for an opponent
     * @param player2            initially null, becomes the matched opponent if
     *                           found
     * @return the opponent (player2) matched with player1
     */
    private TournamentPlayerEloDTO findSuitableOpponent(Map<Long, Set<Long>> playerMatchHistory,
            List<TournamentPlayerEloDTO> group2, boolean[] pairedGroup2, TournamentPlayerEloDTO player1,
            TournamentPlayerEloDTO player2) {
        for (int j = 0; j < group2.size(); j++) {
            if (!pairedGroup2[j] && !playerMatchHistory.getOrDefault(player1.getId(), new HashSet<>())
                    .contains(group2.get(j).getId())) {
                player2 = group2.get(j);
                // Mark player in group2 as paired
                pairedGroup2[j] = true;
                break;
            }
        }
        return player2;
    }

    /**
     * Helper method to create a match entity and add it to the matches list.
     *
     * @param matches      list to add the newly created match
     * @param player1      player one in the match
     * @param player2      player two in the match
     * @param tournamentId the tournament ID
     * @param gameTypeId   the game type ID
     * @param roundNumber  the round number for the match
     */
    private void createAndAddMatch(List<Match> matches, TournamentPlayerEloDTO player1,
            TournamentPlayerEloDTO player2, Long tournamentId, Long gameTypeId, Integer roundNumber) {

        Match match = createMatch(tournamentId, player1.getId(), player2.getId(), null, null, roundNumber,
                getRoundTypeForSwissRound(), gameTypeId, null, Match.MatchStatus.PENDING);
        matches.add(match);
    }

    /**
     * Retrieves the match history for each player in the tournament to avoid
     * repeated pairings.
     *
     * @param tournamentId the ID of the tournament
     * @return a map where each player is mapped to a set of past opponents
     */
    private Map<Long, Set<Long>> getPreviousOpponents(Long tournamentId) {

        List<Match> previousMatches = matchRepository.findByTournamentId(tournamentId);
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

    /**
     * Retrieve the RoundType based on the number of players, used to determine
     * the number of rounds or type of knockout format required for the tournament.
     * 
     * @param numberOfPlayers the number of players in the round
     * @return the RoundType matching the number of players
     */
    private RoundType getRoundType(int numberOfPlayers) {
        return roundTypeRepository.findByNumberOfPlayers(numberOfPlayers)
                .orElseThrow(() -> new RoundTypeNotFoundException(
                        "Round type not found for " + numberOfPlayers + " players"));
    }

    /**
     * Retrieve the RoundType specific to the Swiss tournament format.
     * 
     * @return the RoundType corresponding to a Swiss tournament
     */
    private RoundType getRoundTypeForSwissRound() {
        return roundTypeRepository.findByRoundName("Swiss")
                .orElseThrow(() -> new RoundTypeNotFoundException("Round type not found for Swiss"));
    }

    /**
     * Updates the current round of the tournament by calling the external
     * tournament service.
     *
     * @param tournamentId the ID of the tournament
     * @param roundTypeId  the ID of the round type to set as the current round
     */
    private void updateCurrentRoundForTournament(Long tournamentId, Long roundTypeId) {
        String updateRoundUrl = tournamentServiceUrl + "/api/tournaments/" + tournamentId + "/round/" + roundTypeId;
        // Call the endpoint to update the current round
        restTemplate.put(updateRoundUrl, null);
    }

    /**
     * Retrieve all matches for a specific tournament and convert them into a list
     * of MatchDTO objects.
     *
     * @param tournamentId the tournament ID
     * @return a list of MatchDTO objects for the specified tournament
     */
    public List<MatchDTO> getMatchesByTournament(Long tournamentId) {
        List<Match> matches = matchRepository.findByTournamentId(tournamentId);
        return matches.stream()
                .map(this::convertMatchToMatchDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single match by its ID and converts it to a MatchDTO.
     *
     * @param matchId the ID of the match
     * @return the MatchDTO representation of the match
     */
    public MatchDTO getMatch(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchDoesNotExistException("Match not found"));
        return convertMatchToMatchDTO(match);
    }

    /**
     * Advances the winner of a match, updates Elo ratings, and progresses the
     * tournament
     * to the next round if applicable (Swiss or Knockout).
     *
     * @param matchId  the ID of the match
     * @param winnerId the ID of the winning player
     * @return a message indicating the progress of the tournament
     */
    public String advanceWinner(Long matchId, Long winnerId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchDoesNotExistException("Match not found"));

        validateMatchAndWinner(winnerId, match);

        // Set winner and loser IDs, and mark the match as completed
        match.setWinnerId(winnerId);
        match.setLoserId(match.getPlayer1Id().equals(winnerId) ? match.getPlayer2Id() : match.getPlayer1Id());
        match.setStatus(Match.MatchStatus.COMPLETED);

        // Update Elo of players
        updateMatchEloRequestDTO(match);

        matchRepository.save(match);

        return match.getRoundType().getRoundName().equals("Swiss")
                ? advanceSwissMatch(match, winnerId)
                : advanceKnockoutMatch(match, winnerId);
    }

    /**
     * Validates that the match has not been completed and the winner is a valid
     * participant.
     *
     * @param winnerId the ID of the winner
     * @param match    the match being validated
     */
    private void validateMatchAndWinner(Long winnerId, Match match) {
        if (match.getWinnerId() != null) {
            throw new MatchAlreadyCompletedException("Match has already been completed");
        }

        if (!match.getPlayer1Id().equals(winnerId) && !match.getPlayer2Id().equals(winnerId)) {
            throw new PlayerDoesNotExistInMatchException(
                    "Player with id " + winnerId + " is not recognised in the match.");
        }
    }

    /**
     * Sends a request to update the Elo ratings of players based on the match
     * results.
     *
     * @param match the completed match with a winner and loser
     */
    private void updateMatchEloRequestDTO(Match match) {
        MatchEloRequestDTO matchEloRequestDTO = new MatchEloRequestDTO();
        matchEloRequestDTO.setWinner(match.getWinnerId());
        matchEloRequestDTO.setLoser(match.getLoserId());
        restTemplate.put(eloServiceUrl + "/api/elo/match", matchEloRequestDTO);
    }

    /**
     * Advances a Swiss tournament match, checking if the round is completed and
     * progressing
     * to the next round or moving to knockout phase if the Swiss rounds are
     * finished.
     *
     * @param match    the completed match
     * @param winnerId the ID of the match winner
     * @return a message indicating the progress of the Swiss tournament
     */
    private String advanceSwissMatch(Match match, Long winnerId) {
        SwissBracket bracket = swissBracketRepository.findByTournamentId(match.getTournamentId())
                .orElseThrow(() -> new SwissBracketNotFoundException("Swiss bracket not found"));

        updateSwissStanding(match, winnerId, bracket);

        // Check if the round is completed
        List<Match> roundMatches = matchRepository.findByTournamentIdAndSwissRoundNumber(match.getTournamentId(),
                bracket.getCurrentRound());

        if (isRoundCompleted(roundMatches)) {
            // Check if the last Swiss round is reached
            if (bracket.getCurrentRound() == bracket.getNumberOfRounds()) {
                return completedSwissRound(match, bracket);
            } else {
                return nextSwissRound(match, bracket);
            }
        }
        return "Swiss standings updated";
    }

    /**
     * Updates the Swiss standings for the winner and loser of a match within a
     * Swiss bracket.
     *
     * @param match    the match that was completed
     * @param winnerId the ID of the winning player
     * @param bracket  the Swiss bracket to which the match belongs
     */
    private void updateSwissStanding(Match match, Long winnerId, SwissBracket bracket) {
        SwissStanding winnerStanding = swissStandingRepository.findByBracketIdAndPlayerId(bracket.getId(), winnerId)
                .orElseThrow(() -> new PlayerDoesNotExistInMatchException("Player not found"));

        SwissStanding loserStanding = swissStandingRepository
                .findByBracketIdAndPlayerId(bracket.getId(), match.getLoserId())
                .orElseThrow(() -> new PlayerDoesNotExistInMatchException("Player not found"));

        winnerStanding.setWins(winnerStanding.getWins() + 1);
        loserStanding.setLosses(loserStanding.getLosses() + 1);

        swissStandingRepository.save(winnerStanding);
        swissStandingRepository.save(loserStanding);
    }

    /**
     * Proceeds to the next Swiss round by updating the current round and generating
     * new matches.
     *
     * @param match   the completed match
     * @param bracket the Swiss bracket to advance
     * @return a message indicating advancement to the next Swiss round
     */
    private String nextSwissRound(Match match, SwissBracket bracket) {
        bracket.setCurrentRound(bracket.getCurrentRound() + 1);
        swissBracketRepository.save(bracket);
        // Fetch all players with their updated standings for the next round
        ResponseEntity<TournamentPlayerEloDTO[]> response = restTemplate.exchange(
                tournamentServiceUrl + "/api/tournament-players/" + match.getTournamentId(),
                HttpMethod.GET, null, TournamentPlayerEloDTO[].class);

        TournamentPlayerEloDTO[] allPlayers = response.getBody();

        createRoundMatches(allPlayers, match.getTournamentId(), match.getGameType().getId(),
                bracket.getCurrentRound());
        return "Advanced to Swiss Round " + bracket.getCurrentRound();
    }

    /**
     * Completes the Swiss rounds and initiates the knockout phase for the top
     * players.
     *
     * @param match   the last match in the Swiss phase
     * @param bracket the Swiss bracket for the tournament
     * @return a message indicating the tournament has moved to knockout phase
     */
    private String completedSwissRound(Match match, SwissBracket bracket) {
        // Get the standings for the current bracket and sort by wins and losses for the
        // top half
        List<SwissStanding> standings = swissStandingRepository
                .findByBracketIdOrderByWinsDescLossesAsc(bracket.getId());
        int half = standings.size() / 2;

        // Get the IDs of players who advanced
        List<Long> advancedPlayers = standings.subList(0, half)
                .stream()
                .map(SwissStanding::getPlayerId)
                .collect(Collectors.toList());

        // Create knockout matches for the top half of players
        Long currentRoundId = createKnockoutMatches(match.getTournamentId(), match.getGameType().getId(),
                advancedPlayers);
        updateCurrentRoundForTournament(match.getTournamentId(), currentRoundId);
        return "Swiss rounds completed, moving to knockout phase.";
    }

    /**
     * Advances a knockout match to the next round, progressing through the bracket
     * structure
     * until the final winner is determined.
     *
     * @param match    the completed match
     * @param winnerId the ID of the match winner
     * @return a message indicating the progress of the knockout tournament
     */
    private String advanceKnockoutMatch(Match match, Long winnerId) {
        if (match.getNextMatchId() != null) {

            List<Match> roundMatches = getRoundMatches(match.getTournamentId(), match.getRoundType().getId());

            if (isRoundCompleted(roundMatches)) {
                Match nextMatch = getNextMatch(match.getNextMatchId());
                updateCurrentRoundForTournament(match.getTournamentId(), nextMatch.getRoundType().getId());

                // Assign matches for the next round
                assignNextRoundMatches(getRoundMatches(match.getTournamentId(), match.getRoundType().getId()));

                return "Tournament has advanced to the next round";
            }
            return "Winner advanced to the next round";
        } else {
            updateTournamentWinner(match.getTournamentId(), winnerId);
            return "Tournament completed";
        }
    }

    /**
     * Checks if all matches in a given round are completed.
     *
     * @param roundMatches the list of matches in the round
     * @return true if all matches are completed, false otherwise
     */
    private boolean isRoundCompleted(List<Match> roundMatches) {

        return roundMatches.stream().allMatch(m -> m.getStatus() == Match.MatchStatus.COMPLETED);
    }

    private List<Match> getRoundMatches(Long tournamentId, Long roundTypeId) {
        return matchRepository.findByTournamentIdAndRoundTypeId(tournamentId, roundTypeId);
    }

    private Match getNextMatch(Long nextMatchId) {
        return matchRepository.findById(nextMatchId)
                .orElseThrow(() -> new MatchDoesNotExistException("Next match not found"));
    }

    private void updateTournamentWinner(Long tournamentId, Long winnerId) {
        String updateTournamentUrl = tournamentServiceUrl + "/api/tournaments/" + tournamentId + "/winner/" + winnerId;
        restTemplate.put(updateTournamentUrl, null);
    }

    private void assignNextRoundMatches(List<Match> roundMatches) {
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
    }

    /**
     * Retrieves the top 5 most recent matches played by a player, either as player1
     * or player2.
     *
     * @param playerId the ID of the player
     * @return a list of MatchDTO objects representing recent matches
     */
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

        // Set player details
        matchDTO.setPlayer1(getPlayerDetailsOrNull(match.getPlayer1Id()));
        matchDTO.setPlayer2(getPlayerDetailsOrNull(match.getPlayer2Id()));

        // Set winner and loser IDs
        setWinnerAndLoserIds(match, matchDTO);

        // Set additional match properties
        matchDTO.setSwissRoundNumber(match.getSwissRoundNumber());
        matchDTO.setRoundType(match.getRoundType());
        matchDTO.setGameType(match.getGameType());
        matchDTO.setDate(match.getUpdatedAt());

        // Set tournament details
        matchDTO.setTournament(getTournamentDetails(match.getTournamentId()));

        return matchDTO;
    }

    private PlayerDetailsDTO getPlayerDetailsOrNull(Long playerId) {
        return (playerId != null) ? getPlayerDetails(playerId) : null;
    }

    private void setWinnerAndLoserIds(Match match, MatchDTO matchDTO) {
        matchDTO.setWinnerId(match.getWinnerId());
        matchDTO.setLoserId(match.getLoserId());
    }

    private TournamentDTO getTournamentDetails(Long tournamentId) {
        String tournamentUrl = tournamentServiceUrl + "/api/tournaments/" + tournamentId;
        return restTemplate.getForObject(tournamentUrl, TournamentDTO.class);
    }

    private PlayerDetailsDTO getPlayerDetails(long playerId) {
        String playerUrl = playerServiceUrl + "/api/player/" + playerId;
        return restTemplate.getForObject(playerUrl, PlayerDetailsDTO.class);
    }

    // Create a Match entity with provided details
    private Match createMatch(Long tournamentId, Long player1Id, Long player2Id, Long winnerId, Long loserId,
            Integer swissRoundNumber, RoundType roundType, Long gameTypeId, Long nextMatchId,
            MatchStatus status) {
        Match match = new Match();
        match.setTournamentId(tournamentId);
        match.setPlayer1Id(player1Id);
        match.setPlayer2Id(player2Id);
        match.setWinnerId(winnerId);
        match.setLoserId(loserId);
        match.setSwissRoundNumber(swissRoundNumber);
        match.setRoundType(roundType);
        match.setGameType(gameTypeRepository.findById(gameTypeId)
                .orElseThrow(() -> new GameTypeNotFoundException("GameType with ID " + gameTypeId + " not found")));
        match.setNextMatchId(nextMatchId);
        match.setStatus(status);

        return match;
    }
}
