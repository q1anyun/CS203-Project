package com.chess.tms.user_service.controller;

import com.chess.tms.user_service.dto.AdminRegistrationRequestDTO;
import com.chess.tms.user_service.dto.UserDetailsDTO;
import com.chess.tms.user_service.dto.UserRegistrationRequestDTO;
import com.chess.tms.user_service.dto.UserRegistrationResponseDTO;
import com.chess.tms.user_service.enums.UserRole;
import com.chess.tms.user_service.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserRegistrationRequestDTO userRegistrationRequestDTO;
    private AdminRegistrationRequestDTO adminRegistrationRequestDTO;

    @BeforeEach
    void setUp() {
        // User registration request DTO setup
        userRegistrationRequestDTO = new UserRegistrationRequestDTO(
                "john_doe", "john.doe@example.com", "password123",
                "John", "Doe", UserRole.USER, "profile.jpg"
        );

        // Admin registration request DTO setup
        adminRegistrationRequestDTO = new AdminRegistrationRequestDTO(
                "admin_doe", "admin.doe@example.com", "admin123", UserRole.ADMIN
        );
    }

    // Test user registration endpoint
    @Test
    void testRegisterUser_Success() throws Exception {
        // Mock the service response
        UserRegistrationResponseDTO responseDTO = new UserRegistrationResponseDTO("John", "Doe");

        when(userService.registerUser(any(UserRegistrationRequestDTO.class)))
                .thenReturn(responseDTO);

        // Perform a POST request to the /register/user endpoint with valid input
        mockMvc.perform(post("/api/users/register/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegistrationRequestDTO))
                        .with(csrf())
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"firstName\":\"John\",\"lastName\":\"Doe\"}"));
    }

    // Test admin registration endpoint
    @Test
    void testRegisterAdmin_Success() throws Exception {
        // No return object is needed as the endpoint returns a string

        // Mock the service call
        Mockito.doNothing().when(userService).registerAdmin(any(AdminRegistrationRequestDTO.class));

        // Perform a POST request to the /register/admin endpoint
        mockMvc.perform(post("/api/users/register/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminRegistrationRequestDTO))
                        .with(csrf())
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string("Admin successfully created"));
    }

    // Test getting user details by userId
    @Test
    void testGetUserDetails_Success() throws Exception {
        // Mock the service response
        UserDetailsDTO userDetailsDTO = new UserDetailsDTO(
                1L, 1L, 1200, "John", "Doe", "profile.jpg"
        );

        when(userService.getUserDetailsByUserId(1L)).thenReturn(Optional.of(userDetailsDTO));

        // Perform a GET request to the /{userId}/details endpoint
        mockMvc.perform(get("/api/users/1/details")
                    .with(csrf())
                    .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(userDetailsDTO)));
    }

    // Test getting user details by userId when user not found
    @Test
    void testGetUserDetails_NotFound() throws Exception {
        // Mock the service to return an empty optional
        when(userService.getUserDetailsByUserId(1L)).thenReturn(Optional.empty());

        // Perform a GET request to the /{userId}/details endpoint
        mockMvc.perform(get("/api/users/1/details")
                    .with(csrf())
                    .with(user("user").roles("USER")))
                .andExpect(status().isNotFound());
    }
}