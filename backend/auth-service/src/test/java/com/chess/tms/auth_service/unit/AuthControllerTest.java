package com.chess.tms.auth_service.unit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.chess.tms.auth_service.controller.AuthController;
import com.chess.tms.auth_service.dto.AdminRegistrationRequestDTO;
import com.chess.tms.auth_service.dto.AuthenticatedUserDTO;
import com.chess.tms.auth_service.dto.JwtRequest;
import com.chess.tms.auth_service.dto.PlayerRegistrationRequestDTO;
import com.chess.tms.auth_service.enums.UserRole;
import com.chess.tms.auth_service.security.JwtUtility;
import com.chess.tms.auth_service.service.AuthService;
import com.chess.tms.auth_service.exception.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthControllerTest {
    // Constants
    private static final String VALID_USERNAME = "testuser";
    private static final String VALID_PASSWORD = "password";
    private static final String FAKE_JWT_TOKEN = "fake-jwt-token";

    @Mock
    private AuthService authService;

    @Mock
    private JwtUtility jwtUtility;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // Authentication Tests
    @Test
    void testLogin_WithValidCredentials_ShouldReturnToken() throws Exception {
        // Setup
        AuthenticatedUserDTO authenticatedUser = new AuthenticatedUserDTO();
        authenticatedUser.setUsername(VALID_USERNAME);
        authenticatedUser.setRole(UserRole.PLAYER);

        when(authService.authenticate(any(JwtRequest.class))).thenReturn(authenticatedUser);
        when(jwtUtility.generateToken(any(AuthenticatedUserDTO.class))).thenReturn(FAKE_JWT_TOKEN);

        // Execute & Verify
        mockMvc.perform(post("/api/auth/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + VALID_USERNAME + "\", \"password\":\"" + VALID_PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwtResponse.token").value(FAKE_JWT_TOKEN))
                .andExpect(jsonPath("$.role").value("PLAYER"));
    }

    @Test
    void testLogin_WithInvalidCredentials_ShouldReturnNotFound() throws Exception {
        when(authService.authenticate(any(JwtRequest.class)))
                .thenThrow(new UserNotFoundException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/session")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"wronguser\", \"password\":\"wrongpassword\"}"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Invalid credentials")));
    }

    // Player Registration Tests
    @Test
    void testRegisterPlayer_WithValidInput_ShouldSucceed() throws Exception {
        when(authService.registerPlayer(any(PlayerRegistrationRequestDTO.class)))
                .thenReturn("Player created successfully");

        mockMvc.perform(post("/api/auth/users/player")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"newplayer\", \"email\":\"newplayer@test.com\", \"password\":\"password\", \"country\":\"Singapore\", \"firstName\":\"John\", \"lastName\":\"Doe\", \"profilePicture\":\"picturepng\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Player created successfully"));
    }

    @Test
    void testRegisterPlayer_WithInvalidInput_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/users/player")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"\", \"email\":\"invalid\", \"password\":\"short\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegisterPlayer_WithExistingUser_ShouldReturnConflict() throws Exception {
        when(authService.registerPlayer(any(PlayerRegistrationRequestDTO.class)))
                .thenThrow(new UserAlreadyExistsException("User already exists"));

        mockMvc.perform(post("/api/auth/users/player")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"existinguser\", \"email\":\"existing@test.com\", \"password\":\"password\", \"country\":\"Singapore\", \"firstName\":\"John\", \"lastName\":\"Doe\", \"profilePicture\":\"picturepng\"}"))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("User already exists")));
    }

    // Admin Registration Tests
    @Test
    void testRegisterAdmin_WithValidInput_ShouldSucceed() throws Exception {
        when(authService.registerAdmin(any(AdminRegistrationRequestDTO.class)))
                .thenReturn("Admin created successfully");

        mockMvc.perform(post("/api/auth/users/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"newadmin\", \"email\":\"newadmin@test.com\", \"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Admin created successfully"));
    }

    @Test
    void testRegisterAdmin_WithInvalidInput_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/users/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"\", \"email\":\"invalid\", \"password\":\"short\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegisterAdmin_WithExistingUser_ShouldReturnConflict() throws Exception {
        when(authService.registerAdmin(any(AdminRegistrationRequestDTO.class)))
                .thenThrow(new UserAlreadyExistsException("User already exists"));

        mockMvc.perform(post("/api/auth/users/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"existinguser\", \"email\":\"existing@test.com\", \"password\":\"password\"}"))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("User already exists")));
    }
}
