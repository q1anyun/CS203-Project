package com.chess.tms.user_service.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import com.chess.tms.user_service.dto.UpdateUserRequestDTO;
import com.chess.tms.user_service.dto.UserResponseDTO;
import com.chess.tms.user_service.enums.UserRole;
import com.chess.tms.user_service.model.User;
import com.chess.tms.user_service.repository.UsersRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class UserServiceIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Clean up database
        usersRepository.deleteAll();

        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password123")); // Password will be encoded by the service

        testUser.setRole(UserRole.PLAYER);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        testUser = usersRepository.save(testUser);
    }

    @Test
    void getCurrentUser_ReturnsUser() {
        // Given
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", testUser.getId().toString());
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        // When
        ResponseEntity<UserResponseDTO> response = restTemplate.exchange(
                "/api/user/current",
                HttpMethod.GET,
                requestEntity,
                UserResponseDTO.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserResponseDTO userResponse = response.getBody();
        assertNotNull(userResponse);
        assertEquals(testUser.getUsername(), userResponse.getUsername());
        assertEquals(testUser.getEmail(), userResponse.getEmail());
        assertEquals(testUser.getRole(), userResponse.getRole());
    }

    @Test
    void getAllUsers_ReturnsUsersList() {
        // Given
        User secondUser = new User();
        secondUser.setUsername("seconduser");
        secondUser.setEmail("second@example.com");
        secondUser.setPassword("password456");
        secondUser.setRole(UserRole.PLAYER);
        secondUser.setCreatedAt(LocalDateTime.now());
        secondUser.setUpdatedAt(LocalDateTime.now());
        usersRepository.save(secondUser);

        // When
        ResponseEntity<List<UserResponseDTO>> response = restTemplate.exchange(
                "/api/user",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<UserResponseDTO>>() {
                });

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<UserResponseDTO> users = response.getBody();
        assertNotNull(users);
        assertEquals(2, users.size());
    }

    @Test
    void updateCurrentUser_ValidRequest_Returns200() {
        // Given
        // First verify the user exists
        assertTrue(usersRepository.findById(testUser.getId()).isPresent(), "Test user should exist before update");

        UpdateUserRequestDTO updateRequest = new UpdateUserRequestDTO();
        updateRequest.setEmail("newemail@example.com");
        updateRequest.setRole(UserRole.ADMIN);
        updateRequest.setOldPassword("password123");
        updateRequest.setNewPassword("newpassword");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-Id", testUser.getId().toString());

        HttpEntity<UpdateUserRequestDTO> request = new HttpEntity<>(updateRequest, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/user/current",
                HttpMethod.PUT,
                request,
                String.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Successfully updated credentials", response.getBody());

        // Verify the user was actually updated
        User updatedUser = usersRepository.findById(testUser.getId())
                .orElseThrow(() -> new AssertionError("User should still exist after update"));
        assertEquals("newemail@example.com", updatedUser.getEmail());
        assertEquals(UserRole.ADMIN, updatedUser.getRole());
    }

    @Test
    void updateCurrentUser_InvalidOldPassword_Returns400() {
        // Given
        UpdateUserRequestDTO updateRequest = new UpdateUserRequestDTO();
        updateRequest.setEmail("newemail@example.com");
        updateRequest.setRole(UserRole.ADMIN);
        updateRequest.setOldPassword("password123");
        updateRequest.setNewPassword("password123");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-Id", testUser.getId().toString());
        HttpEntity<UpdateUserRequestDTO> request = new HttpEntity<>(updateRequest, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/user/current",
                HttpMethod.PUT,
                request,
                String.class);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().contains("Password is the same as the current password."));
    }

    // Add a test for missing required fields
    @Test
    void updateCurrentUser_MissingRequiredFields_Returns400() {
        // Given
        UpdateUserRequestDTO updateRequest = new UpdateUserRequestDTO();
        // Don't set any fields to test validation

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-Id", testUser.getId().toString());
        HttpEntity<UpdateUserRequestDTO> request = new HttpEntity<>(updateRequest, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/user/current",
                HttpMethod.PUT,
                request,
                String.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateCurrentUser_NonExistentUser_Returns404() {
        // Given
        UpdateUserRequestDTO updateRequest = new UpdateUserRequestDTO();
        updateRequest.setEmail("newemail@example.com");
        updateRequest.setRole(UserRole.ADMIN);
        updateRequest.setOldPassword("wrongpassword");
        updateRequest.setNewPassword("newpassword");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-Id", "999");
        HttpEntity<UpdateUserRequestDTO> request = new HttpEntity<>(updateRequest, headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/user/current",
                HttpMethod.PUT,
                request,
                String.class);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getCurrentUser_MissingHeader_ReturnsBadRequest() {
        // When
        ResponseEntity<UserResponseDTO> response = restTemplate.exchange(
                "/api/user/current",
                HttpMethod.GET,
                null,
                UserResponseDTO.class);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

}