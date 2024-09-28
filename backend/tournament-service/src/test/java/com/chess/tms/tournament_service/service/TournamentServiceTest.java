package com.chess.tms.tournament_service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.chess.tms.tournament_service.dto.TournamentDetailsDTO;
import com.chess.tms.tournament_service.dto.TournamentRegistrationDTO;
import com.chess.tms.tournament_service.dto.TournamentUpdateRequestDTO;
import com.chess.tms.tournament_service.enums.Status;
import com.chess.tms.tournament_service.exception.TournamentDoesNotExistException;
import com.chess.tms.tournament_service.model.GameType;
import com.chess.tms.tournament_service.model.Tournament;
import com.chess.tms.tournament_service.repository.GameTypeRepository;
import com.chess.tms.tournament_service.repository.TournamentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class TournamentServiceTest {

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private GameTypeRepository gameTypeRepository;

    @InjectMocks
    private TournamentService tournamentService;

    private Tournament tournament;
    private TournamentRegistrationDTO tournamentRegistrationDTO;
    private TournamentUpdateRequestDTO tournamentUpdateRequestDTO;
    private GameType gameType;

    @BeforeEach
    void setUp() {
        gameType = new GameType();
        gameType.setId(1L);
        gameType.setName("CLASSIC");
        gameType.setTimeControlMinutes(60);

        tournament = new Tournament();
        tournament.setTournamentId(1L);
        tournament.setName("Test Tournament");
        tournament.setStatus(Status.UPCOMING);
        tournament.setTimeControl(gameType);

        tournamentRegistrationDTO = new TournamentRegistrationDTO();
        tournamentRegistrationDTO.setName("Test Tournament");
        tournamentRegistrationDTO.setTimeControl(1);

        tournamentUpdateRequestDTO = new TournamentUpdateRequestDTO();
        tournamentUpdateRequestDTO.setName("Updated Tournament");
        tournamentUpdateRequestDTO.setTimeControl(1);
    }

    @Test
    public void testCreateTournament() {
        when(gameTypeRepository.getGameTypeById(anyInt())).thenReturn(gameType);
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        TournamentDetailsDTO createdTournament = tournamentService.createTournament(tournamentRegistrationDTO, 1L);

        assertNotNull(createdTournament);
        assertEquals("Test Tournament", createdTournament.getName());
        verify(tournamentRepository, times(1)).save(any(Tournament.class));
    }

    @Test
    public void testUpdateTournament() {
        when(tournamentRepository.findById(anyLong())).thenReturn(Optional.of(tournament));

        // Ensure updateTournament() is called with the correct DTO type
        tournamentService.updateTournament(1L, tournamentUpdateRequestDTO);

        verify(tournamentRepository, times(1)).save(any(Tournament.class));
    }

    @Test
    public void testGetTournamentDetailsById() {
        when(tournamentRepository.findById(anyLong())).thenReturn(Optional.of(tournament));

        TournamentDetailsDTO tournamentDetails = tournamentService.getTournamentDetailsById(1L);

        assertNotNull(tournamentDetails);
        assertEquals("Test Tournament", tournamentDetails.getName());
        verify(tournamentRepository, times(1)).findById(1L);
    }

    @Test
    public void testDeleteTournament() {
        when(tournamentRepository.findById(anyLong())).thenReturn(Optional.of(tournament));

        TournamentDetailsDTO deletedTournament = tournamentService.deleteTournament(1L);

        assertNotNull(deletedTournament);
        verify(tournamentRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testGetTournamentDetails_NotFound() {
        when(tournamentRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(TournamentDoesNotExistException.class, () -> {
            tournamentService.getTournamentDetailsById(1L);
        });
    }
}