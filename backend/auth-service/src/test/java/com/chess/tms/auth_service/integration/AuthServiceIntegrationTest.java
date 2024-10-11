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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String USERNAME = "newplayer34";
    private static final String PASSWORD = "password";
    private static final String EMAIL = "newplayer34@test.com";

    @BeforeEach
    public void setup() {
        // Clean up database before each test
        usersRepository.deleteAll();
    }

    @Test
    public void testPlayerRegistrationSuccess() throws Exception {
        String registrationJson = createRegistrationJson(USERNAME, EMAIL, PASSWORD, "John", "Doe", "USA", "https://example.com/picture.jpg");

        mockMvc.perform(post("/api/auth/register/player")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registrationJson))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Player created successfully")));
    }

    @Test
    public void testPlayerLoginSuccess() throws Exception {
        String registrationJson = createRegistrationJson(USERNAME, EMAIL, PASSWORD, "John", "Doe", "USA", "https://example.com/picture.jpg");

        mockMvc.perform(post("/api/auth/register/player")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registrationJson))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Player created successfully")));

        JwtRequest jwtRequest = new JwtRequest(USERNAME, PASSWORD);
        String loginJson = objectMapper.writeValueAsString(jwtRequest);

        ResultActions result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.jwtResponse.token").exists())
                .andExpect(jsonPath("$.role").value("PLAYER"));
    }

    @Test
    public void testPlayerLoginFailureInvalidCredentials() throws Exception {
        String registrationJson = createRegistrationJson(USERNAME, EMAIL, PASSWORD, "John", "Doe", "USA", "https://example.com/picture.jpg");

        mockMvc.perform(post("/api/auth/register/player")
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

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidLoginJson))
                .andExpect(status().isForbidden());
    }

    private String createRegistrationJson(String username, String email, String password, String firstName, String lastName, String country, String profilePicture) throws Exception {
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
