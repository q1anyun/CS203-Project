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

    private List<Integer> expectedPlayerChoices;

    @BeforeEach
    void setup() {
        expectedPlayerChoices = List.of(2, 4, 8, 16, 32, 64);  // List.of() is more concise
    }

    @Test
    void getChoicesForNumberOfPlayers_Valid_ReturnChoices() {
        // Arrange: Mock repository to return the expected player choices
        when(roundTypeRepository.findDistinctNumberOfPlayers()).thenReturn(expectedPlayerChoices);

        // Act: Get the actual result from the service
        List<Integer> actualPlayerChoices = roundTypeService.getAvailablePlayerCounts();

        // Assert: Validate the results
        assertPlayerChoices(actualPlayerChoices);
    }

    // Helper method to reduce repetitive assertions
    private void assertPlayerChoices(List<Integer> actualChoices) {
        assertEquals(expectedPlayerChoices.size(), actualChoices.size(), "The number of choices should match.");
        for (int i = 0; i < expectedPlayerChoices.size(); i++) {
            assertEquals(expectedPlayerChoices.get(i), actualChoices.get(i), "Choice at index " + i + " should match.");
        }
    }
}
