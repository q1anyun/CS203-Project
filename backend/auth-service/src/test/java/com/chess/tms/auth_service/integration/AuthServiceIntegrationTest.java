package com.chess.tms.auth_service.integration;

import com.chess.tms.auth_service.dto.JwtRequest;
import com.chess.tms.auth_service.repository.UsersRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Authentication Service.
 * Tests player registration and authentication flows.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthServiceIntegrationTest {

    // Constants
    private static final String USERNAME = "newplayer34";
    private static final String PASSWORD = "password";
    private static final String EMAIL = "newplayer34@test.com";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String COUNTRY = "USA";
    private static final String PROFILE_PICTURE = "https://example.com/picture.jpg";

    // Dependencies
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        usersRepository.deleteAll(); // Ensure clean state before each test
    }

    /**
     * Tests successful player registration with valid credentials and information
     */
    @Test
    public void shouldSuccessfullyRegisterNewPlayer() throws Exception {
        String registrationJson = createRegistrationJson(USERNAME, EMAIL, PASSWORD, 
            FIRST_NAME, LAST_NAME, COUNTRY, PROFILE_PICTURE);

        mockMvc.perform(post("/api/auth/users/player")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registrationJson))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Player created successfully")));
    }

    /**
     * Tests successful player login after registration
     * Verifies JWT token generation and role assignment
     */
    @Test
    public void shouldSuccessfullyLoginRegisteredPlayer() throws Exception {
        // First register the player
        String registrationJson = createRegistrationJson(USERNAME, EMAIL, PASSWORD,
            FIRST_NAME, LAST_NAME, COUNTRY, PROFILE_PICTURE);

        mockMvc.perform(post("/api/auth/users/player")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registrationJson))
                .andExpect(status().isOk());

        // Then attempt login
        JwtRequest jwtRequest = new JwtRequest(USERNAME, PASSWORD);
        String loginJson = objectMapper.writeValueAsString(jwtRequest);

        ResultActions result = mockMvc.perform(post("/api/auth/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.jwtResponse.token").exists())
                .andExpect(jsonPath("$.role").value("PLAYER"));
    }

    /**
     * Tests login failure scenario with invalid credentials
     */
    @Test
    public void shouldRejectLoginWithInvalidCredentials() throws Exception {
        String registrationJson = createRegistrationJson(USERNAME, EMAIL, PASSWORD, "John", "Doe", "USA", "https://example.com/picture.jpg");

        mockMvc.perform(post("/api/auth/users/player")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registrationJson))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Player created successfully")));

        String invalidLoginJson = String.format("""
                {
                "username": "%s",
                "password": "wrongpassword"
                }
                """, USERNAME);

        mockMvc.perform(post("/api/auth/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidLoginJson))
                .andExpect(status().isForbidden());
    }

    // Helper Method to create registration JSON
    private String createRegistrationJson(String username, String email, String password, 
            String firstName, String lastName, String country, String profilePicture) throws Exception {
        return """
                {
                "username": "%s",
                "email": "%s",
                "password": "%s",
                "firstName": "%s",
                "lastName": "%s",
                "country": "%s",
                "profilePicture": "%s"
                }
                """.formatted(username, email, password, firstName, lastName, country, profilePicture);
    }
}
