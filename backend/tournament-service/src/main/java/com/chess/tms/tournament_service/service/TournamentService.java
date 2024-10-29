package com.chess.tms.tournament_service.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.IOException;
import java.time.LocalDate;

import java.util.Comparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.chess.tms.tournament_service.dto.DTOUtil;
import com.chess.tms.tournament_service.dto.PlayerDetailsDTO;
import com.chess.tms.tournament_service.dto.TournamentDetailsDTO;
import com.chess.tms.tournament_service.dto.TournamentRegistrationDTO;
import com.chess.tms.tournament_service.dto.TournamentUpdateRequestDTO;
import com.chess.tms.tournament_service.enums.Format;
import com.chess.tms.tournament_service.enums.Status;
import com.chess.tms.tournament_service.repository.GameTypeRepository;
import com.chess.tms.tournament_service.repository.RoundTypeRepository;
import com.chess.tms.tournament_service.repository.SwissBracketRepository;
import com.chess.tms.tournament_service.repository.TournamentPlayerRepository;
import com.chess.tms.tournament_service.repository.TournamentRepository;
import com.chess.tms.tournament_service.repository.TournamentTypeRepository;

import jakarta.transaction.Transactional;

import com.chess.tms.tournament_service.exception.EloNotInRangeException;
import com.chess.tms.tournament_service.exception.GameTypeNotFoundException;
import com.chess.tms.tournament_service.exception.InsufficientPlayersException;
import com.chess.tms.tournament_service.exception.MatchServiceException;
import com.chess.tms.tournament_service.exception.MaxPlayersReachedException;
import com.chess.tms.tournament_service.exception.PlayerAlreadyRegisteredException;
import com.chess.tms.tournament_service.exception.RoundTypeNotFoundException;
import com.chess.tms.tournament_service.exception.SwissBracketNotFoundException;
import com.chess.tms.tournament_service.exception.TournamentDoesNotExistException;
import com.chess.tms.tournament_service.exception.TournamentTypeNotFoundException;
import com.chess.tms.tournament_service.exception.UserDoesNotExistException;
import com.chess.tms.tournament_service.model.GameType;
import com.chess.tms.tournament_service.model.RoundType;
import com.chess.tms.tournament_service.model.SwissBracket;
import com.chess.tms.tournament_service.model.Tournament;
import com.chess.tms.tournament_service.model.TournamentPlayer;
import com.chess.tms.tournament_service.model.TournamentType;

@Service
public class TournamentService {

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private TournamentPlayerRepository tournamentPlayerRepository;

    @Autowired
    private GameTypeRepository gameTypeRepository;

    @Autowired
    private RoundTypeRepository roundTypeRepository;

    @Autowired
    private TournamentTypeRepository tournamentTypeRepository;

    @Autowired
    private SwissBracketRepository swissBracketRepository;


    @Autowired
    private RestTemplate restTemplate;

    @Value("${matches.service.url}")
    private String matchServiceUrl;

    @Value("${players.service.url}")
    private String playerServiceUrl;
    
    @Value("${s3.upload.service.url}")
    private String s3UploadServiceUrl;

    public String createTournament(TournamentRegistrationDTO dto, long creatorId) {
        Tournament tournament = DTOUtil.convertDTOToTournament(dto, creatorId);
        tournament.setStatus(Status.UPCOMING);
        GameType gameType = gameTypeRepository.getGameTypeById(dto.getTimeControl())
                .orElseThrow(() -> new GameTypeNotFoundException("GameType does not exist."));
        tournament.setTimeControl(gameType);
        tournament.setCurrentPlayers(0);

        TournamentType tournamentType = tournamentTypeRepository.findById(dto.getTournamentType())
                .orElseThrow(() -> new TournamentTypeNotFoundException("TournamentType does not exist."));

        tournament.setTournamentType(tournamentType);

        tournamentRepository.save(tournament);

        return "Tournament created successfully";
    }

    // Start tournament based on tournament type
    public String startTournament(long tournamentId) {
        Tournament tournament = findTournamentById(tournamentId);

        // Check if tournament has enough players to start
        validatePlayerCount(tournament);

        // Start tournament based on tournament type
        switch (tournament.getTournamentType().getTypeName().toLowerCase()) {
            case "swiss":
                startSwissTournament(tournamentId, tournament);
                break;
            case "knockout":
                startKnockoutTournamentAndSetRound(tournament);
                break;
            default:
                throw new TournamentTypeNotFoundException("Invalid TournamentType.");
        }

        // Set tournament status to LIVE and save
        tournament.setStatus(Status.LIVE);
        tournamentRepository.save(tournament);

        return tournament.getName() + " has started and current round is "
                + tournament.getCurrentRound().getRoundName();
    }

    // Fetch tournament by ID or throw an exception
    private Tournament findTournamentById(long tournamentId) {
        return tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentDoesNotExistException(
                        "Tournament with id " + tournamentId + " does not exist."));
    }

    // Validate that the tournament has enough players to start
    private void validatePlayerCount(Tournament tournament) {
        if (tournament.getCurrentPlayers() < 2) {
            throw new InsufficientPlayersException("Tournament cannot start with less than 2 players.");
        }
    }

    // Start Swiss tournament and set current round
    private void startSwissTournament(long tournamentId, Tournament tournament) {
        RoundType roundType = roundTypeRepository.findByRoundName("Swiss")
                .orElseThrow(() -> new RoundTypeNotFoundException("RoundType with name Swiss does not exist."));
        tournament.setCurrentRound(roundType);

        try {
            // Response entity with the Swiss bracket ID from Match Service after generating the matches
            ResponseEntity<Long> responseEntity = restTemplate.postForEntity(
                    matchServiceUrl + "/api/matches/swiss/" + tournamentId + "/" + tournament.getTimeControl().getId(),
                    null, Long.class);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw new MatchServiceException("Failed to start tournament due to match service error: "
                    + ex.getStatusCode() + " - " + ex.getResponseBodyAsString(), ex);
        } catch (RestClientException ex) {
            throw new MatchServiceException("Failed to start tournament due to match service communication error", ex);
        }
    }

    // Start knockout tournament and set round
    private void startKnockoutTournamentAndSetRound(Tournament tournament) {
        ResponseEntity<Long> response = startKnockoutTournament(tournament.getTournamentId(), tournament);
        Long roundTypeId = response.getBody();

        RoundType roundType = roundTypeRepository.findById(roundTypeId)
                .orElseThrow(
                        () -> new RoundTypeNotFoundException("RoundType with id " + roundTypeId + " does not exist."));
        tournament.setCurrentRound(roundType);
    }

    // Start knockout tournament and return the current round ID
    private ResponseEntity<Long> startKnockoutTournament(long tournamentId, Tournament tournament) {
        try {
            return restTemplate.postForEntity(
                    matchServiceUrl + "/api/matches/knockout/" + tournamentId + "/"
                            + tournament.getTimeControl().getId(),
                    null, Long.class);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw new MatchServiceException("Failed to start tournament due to match service error: "
                    + ex.getStatusCode() + " - " + ex.getResponseBodyAsString(), ex);
        } catch (RestClientException ex) {
            throw new MatchServiceException("Failed to start tournament due to match service communication error", ex);
        }
    }

    // Get detailed information about a tournament, including the winner (if any)
    public TournamentDetailsDTO getTournamentDetailsById(long id) {
        Tournament tournament = findTournamentById(id);
        TournamentDetailsDTO returnDTO = convertEntryToDTO(tournament);

        // If there's a winner, fetch winner details
        if (tournament.getWinnerId() != null) {
            PlayerDetailsDTO winner = fetchPlayerDetails(tournament.getWinnerId());
            returnDTO.setWinner(winner);
        }

        return returnDTO;
    }

    @Transactional
    public void deleteTournament(long id) {
        Optional<Tournament> tournamentOptional = tournamentRepository.findById(id);
        if (tournamentOptional.isEmpty()) {
            throw new TournamentDoesNotExistException("Tournament with id " + id + " does not exist.");
        }

        // Tournament tournament = tournamentOptional.get();

        // TournamentDetailsDTO returnDTO = DTOUtil.convertEntryToDTO(tournament);

        tournamentRepository.deleteById(id);
    }

    public List<TournamentDetailsDTO> getAllTournaments() {
        List<Tournament> tournaments = tournamentRepository.findAll();
        List<TournamentDetailsDTO> tournamentDTOs = new ArrayList<>();

        for (Tournament tournament : tournaments) {
            tournamentDTOs.add(convertEntryToDTO(tournament));

            if (tournament.getWinnerId() != null) {
                PlayerDetailsDTO winner = fetchPlayerDetails(tournament.getWinnerId());
                tournamentDTOs.get(tournamentDTOs.size() - 1).setWinner(winner);
            }
        }
        return tournamentDTOs;
    }

    public List<TournamentDetailsDTO> getRecommendedTournaments(long playerId) {
        List<Tournament> tournaments = tournamentRepository.findAll();
        List<TournamentDetailsDTO> tournamentDTOs = new ArrayList<>();

        PlayerDetailsDTO currentPlayer = fetchPlayerDetails(playerId);

        // Filter out tournaments that are not upcoming, is full, or are in the past
        for (Tournament tournament : tournaments) {
            if (currentPlayer.getEloRating() > tournament.getMinElo()
                    && currentPlayer.getEloRating() < tournament.getMaxElo()
                    && tournament.getStatus() == Status.UPCOMING
                    && tournament.getCurrentPlayers() < tournament.getMaxPlayers()
                    && tournament.getStartDate().isAfter(LocalDate.now())) {
                tournamentDTOs.add(convertEntryToDTO(tournament));
            }
        }

        tournamentDTOs.sort(Comparator.comparing(TournamentDetailsDTO::getStartDate));

        return tournamentDTOs;
    }

    public void updateTournament(long id, TournamentUpdateRequestDTO dto) {
        Optional<Tournament> tournamentOptional = tournamentRepository.findById(id);

        if (tournamentOptional.isEmpty()) {
            throw new TournamentDoesNotExistException("Tournament with id " + id + " does not exist.");
        }

        Tournament tournament = tournamentOptional.get();

        if (dto.getName() != null) {
            tournament.setName(dto.getName());
        }
        if (dto.getStartDate() != null) {
            tournament.setStartDate(dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            tournament.setEndDate(dto.getEndDate());
        }
        if (dto.getMinElo() != null) {
            tournament.setMinElo(dto.getMinElo());
        }
        if (dto.getMaxElo() != null) {
            tournament.setMaxElo(dto.getMaxElo());
        }
        if (dto.getMaxPlayers() != null) {
            tournament.setMaxPlayers(dto.getMaxPlayers());
        }
        if (dto.getTimeControl() != null) {
            GameType gameType = gameTypeRepository.getGameTypeById(dto.getTimeControl())
                    .orElseThrow(() -> new GameTypeNotFoundException("GameType does not exist."));
            tournament.setTimeControl(gameType);
        }

        if (dto.getDescription() != null) {
            tournament.setDescription(dto.getDescription());
        }

        if (dto.getPhoto() != null) {
            tournament.setPhoto(dto.getPhoto());
        }

        if (dto.getFormat() != null) {
            if (dto.getFormat().toUpperCase().equals("ONLINE") || dto.getFormat().toUpperCase().equals("HYBRID")
                    || dto.getFormat().toUpperCase().equals("PHYSICAL"))
                tournament.setFormat(Format.valueOf(dto.getFormat().toUpperCase()));
            else
                tournament.setFormat(Format.ONLINE);
        }

        if (dto.getCountry() != null) {
            tournament.setCountry(dto.getCountry());
        }

        if (dto.getLocationAddress() != null) {
            tournament.setLocationAddress(dto.getLocationAddress());
        }

        if (dto.getLocationLatitude() != null) {
            tournament.setLocationLatitude(dto.getLocationLatitude());
        }

        if (dto.getLocationLongitude() != null) {
            tournament.setLocationLongitude(dto.getLocationLongitude());
        }

        // Save the updated tournament
        tournamentRepository.save(tournament);
    }

    public void registerPlayer(long playerId, long tournamentId) {
        Optional<Tournament> tournamentOptional = tournamentRepository.findById(tournamentId);
        if (tournamentOptional.isEmpty()) {
            throw new TournamentDoesNotExistException("Tournament with id " + tournamentId + " does not exist.");
        }

        PlayerDetailsDTO registeringPlayer = fetchPlayerDetails(playerId);

        if (registeringPlayer == null) {
            throw new UserDoesNotExistException("Player with id " + playerId + " does not exist.");
        }

        if (registeringPlayer.getEloRating() < tournamentOptional.get().getMinElo()
                || registeringPlayer.getEloRating() > tournamentOptional.get().getMaxElo()) {
            throw new EloNotInRangeException(
                    "Player with id " + playerId + " does not meet the Elo requirements for this tournament.");
        }

        Tournament tournament = tournamentOptional.get();

        if (tournament.getCurrentPlayers() >= tournament.getMaxPlayers()) {
            throw new MaxPlayersReachedException("Tournament has reached the maximum number of players.");
        }

        Optional<TournamentPlayer> tp = tournamentPlayerRepository.findByPlayerIdAndTournament_TournamentId(playerId,
                tournamentId);

        if (tp.isPresent()) {
            throw new PlayerAlreadyRegisteredException(
                    "Player with id " + playerId + " is already registered for the tournament.");
        }

        TournamentPlayer player = new TournamentPlayer();
        player.setPlayerId(playerId);
        player.setTournament(tournament);

        tournamentPlayerRepository.save(player);

        tournament.setCurrentPlayers(tournament.getCurrentPlayers() + 1);
        tournamentRepository.save(tournament);
    }

    public List<PlayerDetailsDTO> getPlayersByTournament(long tournamentId) {
        Optional<Tournament> tournament = tournamentRepository.findById(tournamentId);

        if (tournament.isEmpty()) {
            throw new TournamentDoesNotExistException("Tournament with id " + tournamentId + " does not exist.");
        }

        List<Long> tournametPlayerIds = tournamentPlayerRepository.findAllByTournament(tournament.get())
                .stream()
                .map(TournamentPlayer::getPlayerId)
                .collect(Collectors.toList());

        ResponseEntity<PlayerDetailsDTO[]> response = restTemplate.postForEntity(
                playerServiceUrl + "/api/player/list",
                tournametPlayerIds,
                PlayerDetailsDTO[].class);

        PlayerDetailsDTO[] playerDetailsArray = response.getBody();

        return Arrays.stream(playerDetailsArray)
                .sorted(Comparator.comparing(PlayerDetailsDTO::getEloRating).reversed())
                .collect(Collectors.toList());

    }

    public String completeTournament(long id, long winnerId) {
        Optional<Tournament> tournamentOptional = tournamentRepository.findById(id);
        if (tournamentOptional.isEmpty()) {
            throw new TournamentDoesNotExistException("Tournament does not exist.");
        }
        Optional<TournamentPlayer> tp = tournamentPlayerRepository.findByPlayerIdAndTournament_TournamentId(winnerId,
                id);

        if (tp.isEmpty()) {
            throw new UserDoesNotExistException("Winner does not exist in this tournament.");
        }

        Tournament tournament = tournamentOptional.get();
        tournament.setWinnerId(winnerId);
        tournament.setStatus(Status.COMPLETED);
        tournamentRepository.save(tournament);

        return tournament.getName() + " has been completed";
    }

    @Transactional
    public void deletePlayerFromTournament(long id, long tournamentid) {
        Optional<Tournament> tournament = tournamentRepository.findById(tournamentid);

        if (tournament.isEmpty()) {
            throw new TournamentDoesNotExistException("Tournament with id " + tournamentid + " does not exist.");
        }

        if (tournament.get().getStatus() == Status.LIVE) {
            throw new TournamentDoesNotExistException("Tournament is Live.");
        }

        Optional<TournamentPlayer> player = tournamentPlayerRepository.findByPlayerIdAndTournament(id,
                tournament.get());

        if (player.isEmpty()) {
            throw new UserDoesNotExistException("Player was not registered for this tournament.");
        }

        tournament.get().setCurrentPlayers(tournament.get().getCurrentPlayers() - 1);

        tournamentRepository.save(tournament.get());

        tournamentPlayerRepository.deleteById(player.get().getId());
    }

    public void updateCurrentRoundForTournament(long tournamentId, long roundTypeId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new TournamentDoesNotExistException(
                        "Tournament with id " + tournamentId + " does not exist."));

        RoundType currentRound = roundTypeRepository.findById(roundTypeId)
                .orElseThrow(
                        () -> new RoundTypeNotFoundException("RoundType with id " + roundTypeId + " does not exist."));

        tournament.setCurrentRound(currentRound);
        tournamentRepository.save(tournament);
    }

    public List<TournamentDetailsDTO> getRegisteredTournaments(long playerId) {
        List<TournamentPlayer> tournamentPlayers = tournamentPlayerRepository.findAllByPlayerId(playerId);
        List<TournamentDetailsDTO> tournamentDetails = new ArrayList<>();

        for (TournamentPlayer tp : tournamentPlayers) {
            tournamentDetails.add(convertEntryToDTO(tp.getTournament()));
        }

        return tournamentDetails;
    }

    public List<TournamentDetailsDTO> getLiveTournaments(long playerId) {

        List<TournamentPlayer> tournamentPlayers = tournamentPlayerRepository.findAllByPlayerId(playerId);
        List<TournamentDetailsDTO> liveTournaments = new ArrayList<>();

        for (TournamentPlayer tp : tournamentPlayers) {
            Tournament tournament = tp.getTournament();
            if (tournament.getStatus() == Status.LIVE) {
                liveTournaments.add(convertEntryToDTO(tournament));
            }
        }

        return liveTournaments;
    }


     // Method to upload a tournament's image
    public void uploadTournamentImage(Long tournamentId, MultipartFile file) throws IOException {
        String url = s3UploadServiceUrl + "/api/s3/upload"; // Adjust based on your actual endpoint

        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Create a multi-value map for the request body
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource()); // Directly use the MultipartFile
        body.add("filename", "tournament_" + tournamentId); // Include the filename as a separate part
        body.add("tournamentId", tournamentId.toString()); // Include the tournament ID in the body if needed

        // Create the request entity
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Make the request
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        // Check response status
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to upload tournament image: " + response.getBody());
        }
    }


     // Method to retrieve a tournament's image by filename
     public byte[] getTournamentImage(String filename) throws IOException {
        String url = s3UploadServiceUrl + "/api/s3/find/" + filename; // Adjust the endpoint to match the server configuration for finding tournament images

        // Make the request to retrieve the file
        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, null, byte[].class);

        // Check response status
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to retrieve tournament image: " + response.getStatusCode());
        }

        // Return the byte array
        return response.getBody();
    }

    // Helper method to fetch player details from the player service
    private PlayerDetailsDTO fetchPlayerDetails(Long playerId) {
        ResponseEntity<PlayerDetailsDTO> response = restTemplate.getForEntity(
                playerServiceUrl + "/api/player/" + playerId, PlayerDetailsDTO.class);
        return response.getBody();
    }

    public TournamentDetailsDTO convertEntryToDTO(Tournament tournament) {
        TournamentDetailsDTO dto = new TournamentDetailsDTO();
        dto.setId(tournament.getTournamentId());
        dto.setName(tournament.getName());
        dto.setDescription(tournament.getDescription());
        dto.setPhoto(tournament.getPhoto());
        dto.setFormat(tournament.getFormat());
        dto.setCountry(tournament.getCountry());
        dto.setLocationAddress(tournament.getLocationAddress());
        dto.setLocationLatitude(tournament.getLocationLatitude());
        dto.setLocationLongitude(tournament.getLocationLongitude());
        dto.setStartDate(tournament.getStartDate());
        dto.setEndDate(tournament.getEndDate());
        dto.setMinElo(tournament.getMinElo());
        dto.setMaxElo(tournament.getMaxElo());
        dto.setCurrentPlayers(tournament.getCurrentPlayers());
        dto.setMaxPlayers(tournament.getMaxPlayers());
        dto.setStatus(tournament.getStatus());
        dto.setTimeControl(tournament.getTimeControl());
        dto.setCurrentRound(tournament.getCurrentRound());
        dto.setCreatorId(tournament.getCreatorId());
        dto.setTournamentType(tournament.getTournamentType());

        Optional<SwissBracket> bracket = swissBracketRepository.findByTournament(tournament);

        bracket.ifPresent(b -> dto.setSwissBracketId(b.getId()));
        return dto;
    }
    
}
