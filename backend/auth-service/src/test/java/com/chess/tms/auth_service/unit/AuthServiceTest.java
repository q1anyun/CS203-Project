package com.chess.tms.auth_service.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.chess.tms.auth_service.dto.AdminRegistrationRequestDTO;
import com.chess.tms.auth_service.dto.AuthenticatedUserDTO;
import com.chess.tms.auth_service.dto.JwtRequest;
import com.chess.tms.auth_service.dto.PlayerRegistrationRequestDTO;
import com.chess.tms.auth_service.enums.UserRole;
import com.chess.tms.auth_service.exception.UserAlreadyExistsException;
import com.chess.tms.auth_service.exception.UserNotFoundException;
import com.chess.tms.auth_service.model.PlayerDetails;
import com.chess.tms.auth_service.model.User;
import com.chess.tms.auth_service.repository.PlayerDetailsRepository;
import com.chess.tms.auth_service.repository.UsersRepository;
import com.chess.tms.auth_service.service.AuthService;

import java.util.Optional;

public class AuthServiceTest {
    @Mock
    private UsersRepository usersRepository;

    @Mock
    private PlayerDetailsRepository playerDetailsRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    // Test fixtures
    private User mockUser;
    private PlayerDetails mockPlayerDetails;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize mock user with common test data
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("username");
        mockUser.setEmail("email@email.com");
        mockUser.setPassword("password");
        mockUser.setRole(UserRole.PLAYER);

        // Initialize mock player details with common test data
        mockPlayerDetails = new PlayerDetails();
        mockPlayerDetails.setId(1L);
        mockPlayerDetails.setUserId(1L);
        mockPlayerDetails.setEloRating(500);
        mockPlayerDetails.setFirstName("Test");
        mockPlayerDetails.setLastName("User");
    }

    // Authentication Tests
    @Test
    void testAuthenticateSuccess() {
        JwtRequest jwtRequest = new JwtRequest("username", "password");

        when(usersRepository.findByUsername("username")).thenReturn(Optional.of(mockUser));
        when(playerDetailsRepository.findByUserId(1L)).thenReturn(Optional.of(mockPlayerDetails));

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);

        AuthenticatedUserDTO result = authService.authenticate(jwtRequest);

        assertEquals("username", result.getUsername());
        assertEquals(mockUser.getEmail(), result.getEmail());
        assertEquals(mockUser.getRole(), result.getRole());
        assertEquals(1L, result.getPlayerId());
    }

    @Test
    void testAuthenticateUserNotFound() {
        JwtRequest jwtRequest = new JwtRequest("invaliduser", "password");

        when(usersRepository.findByUsername("invaliduser")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.authenticate(jwtRequest));
    }

    @Test
    void testAuthenticatePlayerNotFound() {
        JwtRequest jwtRequest = new JwtRequest("invalidplayer", "password");

        when(usersRepository.findByUsername("invalidplayer")).thenReturn(Optional.of(mockUser));

        when(playerDetailsRepository.findByUserId(mockUser.getId())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.authenticate(jwtRequest));
    }

    // Player Registration Tests
    @Test
    void testRegisterPlayerSuccess() {
        PlayerRegistrationRequestDTO playerRequest = createPlayerRegistrationRequest();

        // Mock repository responses
        when(usersRepository.findByUsername("newplayer")).thenReturn(Optional.empty());
        when(usersRepository.findByEmail("newplayer@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(usersRepository.save(any(User.class))).thenReturn(createMockSavedUser());
        when(playerDetailsRepository.save(any(PlayerDetails.class))).thenReturn(createMockPlayerDetails());

        String result = authService.registerPlayer(playerRequest);

        assertEquals("Player created successfully", result);
        verifyPlayerRegistrationCalls();
    }

    // Admin Registration Tests
    @Test
    void testRegisterAdminSuccess() {
        AdminRegistrationRequestDTO admin = createAdminRegistrationRequest();

        // Mock repository responses
        when(usersRepository.findByUsername("newadmin")).thenReturn(Optional.empty());
        when(usersRepository.findByEmail("newadmin@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(usersRepository.save(any(User.class))).thenReturn(createMockSavedAdmin(admin));

        String result = authService.registerAdmin(admin);

        assertEquals("Admin created successfully", result);
        verify(usersRepository, times(1)).save(any(User.class));
    }

    // Helper methods for creating test objects
    private PlayerRegistrationRequestDTO createPlayerRegistrationRequest() {
        PlayerRegistrationRequestDTO request = new PlayerRegistrationRequestDTO();
        request.setUsername("newplayer");
        request.setEmail("newplayer@test.com");
        request.setPassword("password");
        request.setFirstName("New");
        request.setLastName("Player");
        request.setCountry("USA");
        return request;
    }

    private User createMockSavedUser() {
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("newplayer");
        savedUser.setEmail("newplayer@test.com");
        savedUser.setPassword("hashedPassword");
        savedUser.setRole(UserRole.PLAYER);
        return savedUser;
    }

    private PlayerDetails createMockPlayerDetails() {
        PlayerDetails playerDetails = new PlayerDetails();
        playerDetails.setId(1L);
        playerDetails.setUserId(1L);
        return playerDetails;
    }

    private AdminRegistrationRequestDTO createAdminRegistrationRequest() {
        AdminRegistrationRequestDTO admin = new AdminRegistrationRequestDTO();
        admin.setEmail("newadmin@test.com");
        admin.setUsername("newadmin");
        admin.setPassword("password");
        return admin;
    }

    private User createMockSavedAdmin(AdminRegistrationRequestDTO admin) {
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername(admin.getUsername());
        savedUser.setEmail(admin.getEmail());
        savedUser.setPassword("hashedPassword");
        savedUser.setRole(UserRole.ADMIN);
        return savedUser;
    }

    private void verifyPlayerRegistrationCalls() {
        verify(usersRepository, times(1)).save(any(User.class));
        verify(playerDetailsRepository, times(1)).save(any(PlayerDetails.class));
    }

    @Test
    void testRegisterPlayerUsernameExists() {
        PlayerRegistrationRequestDTO playerRequest = new PlayerRegistrationRequestDTO();
        playerRequest.setUsername("username");

        when(usersRepository.findByUsername("username")).thenReturn(Optional.of(mockUser));

        assertThrows(UserAlreadyExistsException.class, () -> authService.registerPlayer(playerRequest));
    }

    @Test
    void testRegisterPlayerEmailExists() {
        PlayerRegistrationRequestDTO playerRequest = new PlayerRegistrationRequestDTO();
        playerRequest.setEmail("existing@test.com");

        when(usersRepository.findByEmail("existing@test.com")).thenReturn(Optional.of(mockUser));

        assertThrows(UserAlreadyExistsException.class, () -> authService.registerPlayer(playerRequest));
    }

    @Test
    void testRegisterAdminUsernameExists() {
        AdminRegistrationRequestDTO adminRequest = new AdminRegistrationRequestDTO();
        adminRequest.setUsername("username");

        when(usersRepository.findByUsername("username")).thenReturn(Optional.of(mockUser));

        assertThrows(UserAlreadyExistsException.class, () -> authService.registerAdmin(adminRequest));
    }

    @Test
    void testRegisterAdminEmailExists() {
        AdminRegistrationRequestDTO adminRequest = new AdminRegistrationRequestDTO();
        adminRequest.setEmail("existing@test.com");

        when(usersRepository.findByEmail("existing@test.com")).thenReturn(Optional.of(mockUser));

        assertThrows(UserAlreadyExistsException.class, () -> authService.registerAdmin(adminRequest));
    }
}
