package com.chess.tms.user_service.unit.service;

import com.chess.tms.user_service.dto.UpdateUserRequestDTO;
import com.chess.tms.user_service.dto.UserResponseDTO;
import com.chess.tms.user_service.enums.UserRole;
import com.chess.tms.user_service.exception.UserAlreadyExistsException;
import com.chess.tms.user_service.exception.UserNotFoundException;
import com.chess.tms.user_service.model.User;
import com.chess.tms.user_service.repository.UsersRepository;
import com.chess.tms.user_service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UsersRepository usersRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User existingUser;
    private UpdateUserRequestDTO updateUserRequestDTO;
    private static final Long USER_ID = 1L;
    private static final String USERNAME = "testUser";
    private static final String OLD_EMAIL = "old@example.com";
    private static final String NEW_EMAIL = "new@example.com";
    private static final String OLD_PASSWORD = "oldPassword";
    private static final String NEW_PASSWORD = "newPassword";
    private static final String ENCODED_OLD_PASSWORD = "encodedOldPassword";
    private static final String ENCODED_NEW_PASSWORD = "encodedNewPassword";

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setId(USER_ID);
        existingUser.setEmail(OLD_EMAIL);
        existingUser.setUsername(USERNAME);
        existingUser.setPassword(ENCODED_OLD_PASSWORD);
        existingUser.setRole(UserRole.ADMIN);

        updateUserRequestDTO = new UpdateUserRequestDTO();
    }

    @Test
    void getUser_UserExists_ReturnsUserResponseDTO() {
        // Arrange
        when(usersRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));

        // Act
        UserResponseDTO response = userService.getUser(USER_ID);

        // Assert
        assertNotNull(response);
        assertEquals(USERNAME, response.getUsername());
        assertEquals(OLD_EMAIL, response.getEmail());
        assertEquals(UserRole.ADMIN, response.getRole());

        verify(usersRepository).findById(USER_ID);
        verifyNoMoreInteractions(usersRepository);
    }

    @Test
    void getUser_UserNotFound_ThrowsUserNotFoundException() {
        // Arrange
        when(usersRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.getUser(USER_ID));

        verify(usersRepository).findById(USER_ID);
        verifyNoMoreInteractions(usersRepository);
    }

    @Test
    void getAllUsers_UsersExist_ReturnsListOfUserResponseDTO() {
        // Arrange
        User secondUser = new User();
        secondUser.setId(2L);
        secondUser.setUsername("secondUser");
        secondUser.setEmail("second@example.com");
        secondUser.setRole(UserRole.ADMIN);

        List<User> userList = Arrays.asList(existingUser, secondUser);
        when(usersRepository.findAll()).thenReturn(userList);

        // Act
        List<UserResponseDTO> response = userService.getAllUsers();

        // Assert
        assertNotNull(response);
        assertEquals(2, response.size());

        // Verify first user
        UserResponseDTO firstUserResponse = response.get(0);
        assertEquals(USERNAME, firstUserResponse.getUsername());
        assertEquals(OLD_EMAIL, firstUserResponse.getEmail());
        assertEquals(UserRole.ADMIN, firstUserResponse.getRole());

        // Verify second user
        UserResponseDTO secondUserResponse = response.get(1);
        assertEquals("secondUser", secondUserResponse.getUsername());
        assertEquals("second@example.com", secondUserResponse.getEmail());
        assertEquals(UserRole.ADMIN, secondUserResponse.getRole());

        verify(usersRepository).findAll();
        verifyNoMoreInteractions(usersRepository);
    }

    @Test
    void getAllUsers_NoUsers_ReturnsEmptyList() {
        // Arrange
        when(usersRepository.findAll()).thenReturn(List.of());

        // Act
        List<UserResponseDTO> response = userService.getAllUsers();

        // Assert
        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(usersRepository).findAll();
        verifyNoMoreInteractions(usersRepository);
    }

    @Test
    void updateUser_UserNotFound_ThrowsUserNotFoundException() {
        // Arrange
        when(usersRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class,
                () -> userService.updateUser(USER_ID, updateUserRequestDTO));

        verify(usersRepository).findById(USER_ID);
        verifyNoMoreInteractions(usersRepository);
    }

    @Test
    void updateUser_IncorrectOldPassword_ThrowsUserNotFoundException() {
        // Arrange
        when(usersRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(OLD_PASSWORD, ENCODED_OLD_PASSWORD)).thenReturn(false);

        updateUserRequestDTO.setOldPassword(OLD_PASSWORD);

        // Act & Assert
        assertThrows(UserNotFoundException.class,
                () -> userService.updateUser(USER_ID, updateUserRequestDTO));

        verify(usersRepository).findById(USER_ID);
        verify(passwordEncoder).matches(OLD_PASSWORD, ENCODED_OLD_PASSWORD);
        verifyNoMoreInteractions(usersRepository);
    }

    @Test
    void updateUser_SameEmail_ThrowsUserAlreadyExistsException() {
        // Arrange
        when(usersRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
        updateUserRequestDTO.setEmail(OLD_EMAIL);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class,
                () -> userService.updateUser(USER_ID, updateUserRequestDTO));

        verify(usersRepository).findById(USER_ID);
        verifyNoMoreInteractions(usersRepository);
    }

    @Test
    void updateUser_EmailAlreadyExists_ThrowsUserAlreadyExistsException() {
        // Arrange
        when(usersRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
        when(usersRepository.findByEmail(NEW_EMAIL)).thenReturn(Optional.of(new User()));

        updateUserRequestDTO.setEmail(NEW_EMAIL);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class,
                () -> userService.updateUser(USER_ID, updateUserRequestDTO));

        verify(usersRepository).findById(USER_ID);
        verify(usersRepository).findByEmail(NEW_EMAIL);
        verifyNoMoreInteractions(usersRepository);
    }

    @Test
    void updateUser_SamePassword_ThrowsUserAlreadyExistsException() {
        // Arrange
        when(usersRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(NEW_PASSWORD, ENCODED_OLD_PASSWORD)).thenReturn(true);

        updateUserRequestDTO.setNewPassword(NEW_PASSWORD);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class,
                () -> userService.updateUser(USER_ID, updateUserRequestDTO));

        verify(usersRepository).findById(USER_ID);
        verify(passwordEncoder).matches(NEW_PASSWORD, ENCODED_OLD_PASSWORD);
        verifyNoMoreInteractions(usersRepository);
    }

    @Test
    void updateUser_SuccessfulUpdate_AllFields() {
        // Arrange
        when(usersRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
        when(usersRepository.findByEmail(NEW_EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.matches(OLD_PASSWORD, ENCODED_OLD_PASSWORD)).thenReturn(true);
        when(passwordEncoder.matches(NEW_PASSWORD, ENCODED_OLD_PASSWORD)).thenReturn(false);
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(ENCODED_NEW_PASSWORD);

        updateUserRequestDTO.setOldPassword(OLD_PASSWORD);
        updateUserRequestDTO.setNewPassword(NEW_PASSWORD);
        updateUserRequestDTO.setEmail(NEW_EMAIL);
        updateUserRequestDTO.setRole(UserRole.ADMIN);

        // Act
        userService.updateUser(USER_ID, updateUserRequestDTO);

        // Assert
        verify(usersRepository).findById(USER_ID);
        verify(usersRepository).findByEmail(NEW_EMAIL);
        verify(passwordEncoder).matches(OLD_PASSWORD, ENCODED_OLD_PASSWORD);
        verify(passwordEncoder).matches(NEW_PASSWORD, ENCODED_OLD_PASSWORD);
        verify(passwordEncoder).encode(NEW_PASSWORD);
        verify(usersRepository).save(any(User.class));

        assertEquals(NEW_EMAIL, existingUser.getEmail());
        assertEquals(ENCODED_NEW_PASSWORD, existingUser.getPassword());
        assertEquals(UserRole.ADMIN, existingUser.getRole());
        assertNotNull(existingUser.getUpdatedAt());
    }

    @Test
    void updateUser_SuccessfulUpdate_OnlyEmail() {
        // Arrange
        when(usersRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
        when(usersRepository.findByEmail(NEW_EMAIL)).thenReturn(Optional.empty());

        updateUserRequestDTO.setEmail(NEW_EMAIL);

        // Act
        userService.updateUser(USER_ID, updateUserRequestDTO);

        // Assert
        verify(usersRepository).findById(USER_ID);
        verify(usersRepository).findByEmail(NEW_EMAIL);
        verify(usersRepository).save(any(User.class));

        assertEquals(NEW_EMAIL, existingUser.getEmail());
        assertEquals(ENCODED_OLD_PASSWORD, existingUser.getPassword());
        assertEquals(UserRole.ADMIN, existingUser.getRole());
        assertNotNull(existingUser.getUpdatedAt());
    }

    @Test
    void updateUser_EmptyNewPassword_ShouldNotUpdatePassword() {
        // Arrange
        when(usersRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
        when(usersRepository.findByEmail(NEW_EMAIL)).thenReturn(Optional.empty());

        // Set empty password and new email
        updateUserRequestDTO.setNewPassword(""); // empty password
        updateUserRequestDTO.setEmail(NEW_EMAIL); // valid email change

        // Act
        userService.updateUser(USER_ID, updateUserRequestDTO);

        // Assert
        verify(usersRepository).findById(USER_ID);
        verify(usersRepository).findByEmail(NEW_EMAIL);
        verify(usersRepository).save(any(User.class));

        // Password encoder should never be called for empty password
        verify(passwordEncoder, never()).encode(anyString());

        // Verify password remained unchanged but email was updated
        assertEquals(ENCODED_OLD_PASSWORD, existingUser.getPassword());
        assertEquals(NEW_EMAIL, existingUser.getEmail());
        assertNotNull(existingUser.getUpdatedAt());
    }

    @Test
    void updateUser_EmptyEmail_ShouldNotUpdateEmail() {
        // Arrange
        when(usersRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(NEW_PASSWORD, ENCODED_OLD_PASSWORD)).thenReturn(false);
        when(passwordEncoder.encode(NEW_PASSWORD)).thenReturn(ENCODED_NEW_PASSWORD);

        // Set empty email but valid password
        updateUserRequestDTO.setEmail(""); // empty email
        updateUserRequestDTO.setNewPassword(NEW_PASSWORD); // valid password change

        // Act
        userService.updateUser(USER_ID, updateUserRequestDTO);

        // Assert
        verify(usersRepository).findById(USER_ID);
        verify(usersRepository, never()).findByEmail(anyString()); // email check should never happen
        verify(passwordEncoder).matches(NEW_PASSWORD, ENCODED_OLD_PASSWORD);
        verify(passwordEncoder).encode(NEW_PASSWORD);
        verify(usersRepository).save(any(User.class));

        // Verify email remained unchanged but password was updated
        assertEquals(OLD_EMAIL, existingUser.getEmail());
        assertEquals(ENCODED_NEW_PASSWORD, existingUser.getPassword());
        assertNotNull(existingUser.getUpdatedAt());
    }

    @Test
    void updateUser_EmptyOldPassword_ShouldSkipPasswordValidation() {
        // Arrange
        when(usersRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser));
        when(usersRepository.findByEmail(NEW_EMAIL)).thenReturn(Optional.empty());

        // Set empty old password but provide new email
        updateUserRequestDTO.setOldPassword(""); // empty old password
        updateUserRequestDTO.setEmail(NEW_EMAIL); // valid email change

        // Act
        userService.updateUser(USER_ID, updateUserRequestDTO);

        // Assert
        verify(usersRepository).findById(USER_ID);
        verify(usersRepository).findByEmail(NEW_EMAIL);
        verify(passwordEncoder, never()).matches(anyString(), anyString()); // password validation should be skipped
        verify(usersRepository).save(any(User.class));

        // Verify email was updated despite empty old password
        assertEquals(NEW_EMAIL, existingUser.getEmail());
        assertNotNull(existingUser.getUpdatedAt());
    }

}
