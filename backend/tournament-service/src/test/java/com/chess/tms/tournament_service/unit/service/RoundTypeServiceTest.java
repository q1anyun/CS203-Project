package com.chess.tms.tournament_service.unit.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.chess.tms.tournament_service.repository.RoundTypeRepository;
import com.chess.tms.tournament_service.service.RoundTypeService;

@ExtendWith(MockitoExtension.class)
class RoundTypeServiceTest {

    @Mock
    private RoundTypeRepository roundTypeRepository;

    @InjectMocks
    private RoundTypeService roundTypeService;

    private List<Integer> numberOfPlayersChoices;

    @BeforeEach
    void setup() {
        numberOfPlayersChoices = List.of(2, 4, 8, 16, 32, 64);
    }

    @Test
    void getChoicesForNumberOfPlayers_ReturnsExpectedChoices() {
        when(roundTypeRepository.findDistinctNumberOfPlayers()).thenReturn(numberOfPlayersChoices);

        List<Integer> result = roundTypeService.getChoicesForNumberOfPlayers();

        assertAll("numberOfPlayersChoices",
            () -> assertEquals(6, result.size(), "Size should match"),
            () -> assertEquals(2, result.get(0), "First element should be 2"),
            () -> assertEquals(16, result.get(3), "Fourth element should be 16"),
            () -> assertEquals(64, result.get(5), "Last element should be 64")
        );
    }
}
