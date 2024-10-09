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

    @Test
    void testLogin() throws Exception {
        AuthenticatedUserDTO authenticatedUser = new AuthenticatedUserDTO();
        authenticatedUser.setUsername("testuser");
        authenticatedUser.setRole(UserRole.PLAYER);

        when(authService.authenticate(any(JwtRequest.class))).thenReturn(authenticatedUser);
        when(jwtUtility.generateToken(any(AuthenticatedUserDTO.class))).thenReturn("fake-jwt-token");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"testuser\", \"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwtResponse.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.role").value("PLAYER"));
    }

    @Test
    void testRegisterPlayer() throws Exception {
        when(authService.registerPlayer(any(PlayerRegistrationRequestDTO.class)))
                .thenReturn("Player created successfully");

        mockMvc.perform(post("/api/auth/register/player")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"newplayer\", \"email\":\"newplayer@test.com\", \"password\":\"password\", \"country\":\"Singapore\", \"firstName\":\"John\", \"lastName\":\"Doe\", \"profilePicture\":\"picturepng\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Player created successfully"));
    }
    
    @Test
    void testRegisterAdmin() throws Exception {
        when(authService.registerAdmin(any(AdminRegistrationRequestDTO.class)))
                .thenReturn("Admin created successfully");

        mockMvc.perform(post("/api/auth/register/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"newadmin\", \"email\":\"newadmin@test.com\", \"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Admin created successfully"));
    }

    @Test
    void testLoginInvalidCredentials() throws Exception {
        when(authService.authenticate(any(JwtRequest.class)))
                .thenThrow(new UserNotFoundException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"wronguser\", \"password\":\"wrongpassword\"}"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Invalid credentials")));
    }

    @Test
    void testRegisterPlayerInvalidInput() throws Exception {
        mockMvc.perform(post("/api/auth/register/player")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"\", \"email\":\"invalid\", \"password\":\"short\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegisterAdminInvalidInput() throws Exception {
        mockMvc.perform(post("/api/auth/register/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"\", \"email\":\"invalid\", \"password\":\"short\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegisterPlayerUserAlreadyExists() throws Exception {
        when(authService.registerPlayer(any(PlayerRegistrationRequestDTO.class)))
                .thenThrow(new UserAlreadyExistsException("User already exists"));

        mockMvc.perform(post("/api/auth/register/player")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"existinguser\", \"email\":\"existing@test.com\", \"password\":\"password\", \"country\":\"Singapore\", \"firstName\":\"John\", \"lastName\":\"Doe\", \"profilePicture\":\"picturepng\"}"))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("User already exists")));
    }

    @Test
    void testRegisterAdminUserAlreadyExists() throws Exception {
        when(authService.registerAdmin(any(AdminRegistrationRequestDTO.class)))
                .thenThrow(new UserAlreadyExistsException("User already exists"));

        mockMvc.perform(post("/api/auth/register/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"existinguser\", \"email\":\"existing@test.com\", \"password\":\"password\"}"))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("User already exists")));
    }

}
