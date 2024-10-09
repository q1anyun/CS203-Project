package com.chess.tms.auth_service.integration;

import com.chess.tms.auth_service.dto.JwtRequest;
import com.chess.tms.auth_service.dto.PlayerRegistrationRequestDTO;
import com.chess.tms.auth_service.model.User;
import com.chess.tms.auth_service.repository.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthServiceIntegrationTest {

        @Container
        public static MySQLContainer<?> mysqlDB = new MySQLContainer<>("mysql:8.0.34")
                        .withDatabaseName("chess_tms_test")
                        .withUsername("root-test")
                        .withPassword("pass-test");

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private UsersRepository usersRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

            @DynamicPropertySource
    static void setDatasourceProperties(org.springframework.test.context.DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlDB::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlDB::getUsername);
        registry.add("spring.datasource.password", mysqlDB::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }


        @BeforeEach
        public void setup() {
                // Clean up database before each test
                usersRepository.deleteAll();
        }

        @Test
        public void testPlayerRegistrationSuccess() throws Exception {
                String registrationJson = """
                                {
                                "username": "newplayer",
                                "email": "newplayer@test.com",
                                "password": "password",
                                "firstName": "John",
                                "lastName": "Doe",
                                "country": "USA"
                                }
                                """;

                // Perform HTTP POST request for player registration
                mockMvc.perform(post("/api/auth/register/player")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registrationJson))
                                .andExpect(status().isOk())
                                .andExpect(content().string(containsString("Player created successfully")));
        }

        @Test
        public void testPlayerLoginSuccess() throws Exception {
                // Create a user in the database
                User user = new User();
                user.setUsername("existingplayer");
                user.setEmail("existingplayer@test.com");
                user.setPassword(passwordEncoder.encode("password"));
                usersRepository.save(user);

                // Prepare JWT request DTO
                JwtRequest jwtRequest = new JwtRequest();
                jwtRequest.setUsername("existingplayer");
                jwtRequest.setPassword("password");

                // Convert DTO to JSON string
                String loginJson = """
                                {
                                "username": "existingplayer",
                                "password": "password"
                                }
                                """;

                // Perform HTTP POST request for login
                ResultActions result = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginJson));

                // Validate the response
                result.andExpect(status().isOk())
                                .andExpect(jsonPath("$.jwtResponse.token").exists())
                                .andExpect(jsonPath("$.role").value("PLAYER"));
        }

        @Test
        public void testPlayerLoginFailureInvalidCredentials() throws Exception {
                // Create a user in the database
                User user = new User();
                user.setUsername("existingplayer");
                user.setEmail("existingplayer@test.com");
                user.setPassword(passwordEncoder.encode("password"));
                usersRepository.save(user);

                // Invalid login request
                String invalidLoginJson = """
                                {
                                "username": "existingplayer",
                                "password": "wrongpassword"
                                }
                                """;

                // Perform HTTP POST request for login with wrong password
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidLoginJson))
                                .andExpect(status().isUnauthorized())
                                .andExpect(content().string(containsString("Bad credentials")));
        }
}