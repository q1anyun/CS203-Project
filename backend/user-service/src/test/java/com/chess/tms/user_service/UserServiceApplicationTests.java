package com.chess.tms.user_service;

import com.chess.tms.user_service.dto.UpdateUserRequestDTO;
import com.chess.tms.user_service.enums.UserRole;
import com.chess.tms.user_service.exception.UserAlreadyExistsException;
import com.chess.tms.user_service.exception.UserNotFoundException;
import com.chess.tms.user_service.model.User;
import com.chess.tms.user_service.repository.UsersRepository;
import com.chess.tms.user_service.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("john_doe");
        existingUser.setEmail("john.doe@example.com");
        existingUser.setPassword("hashed_password");
        existingUser.setRole(UserRole.PLAYER);

        updateUserRequestDTO = new UpdateUserRequestDTO();
        updateUserRequestDTO.setEmail("new.email@example.com");
        updateUserRequestDTO.setPassword("new_password");
        updateUserRequestDTO.setRole(UserRole.ADMIN);
    }

    @Test
    void updateUser_shouldUpdateEmailAndPasswordAndRole() {
        // Arrange
        when(usersRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));
        when(usersRepository.findByEmail(updateUserRequestDTO.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(updateUserRequestDTO.getPassword())).thenReturn("encoded_password");

        // Act
        userService.updateUser(existingUser.getId(), updateUserRequestDTO);

        // Assert
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(usersRepository).save(captor.capture());

        User updatedUser = captor.getValue();
        assertEquals(updateUserRequestDTO.getEmail(), updatedUser.getEmail());
        assertEquals("encoded_password", updatedUser.getPassword());
        assertEquals(updateUserRequestDTO.getRole(), updatedUser.getRole());
        assertNotNull(updatedUser.getUpdatedAt());
    }

    @Test
    void updateUser_shouldThrowUserNotFoundException_whenUserNotFound() {
        // Arrange
        when(usersRepository.findById(existingUser.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.updateUser(existingUser.getId(), updateUserRequestDTO));
    }

    @Test
    void updateUser_shouldThrowUserAlreadyExistsException_whenEmailAlreadyExists() {
        // Arrange
        when(usersRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));
        when(usersRepository.findByEmail(updateUserRequestDTO.getEmail())).thenReturn(Optional.of(new User()));

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> userService.updateUser(existingUser.getId(), updateUserRequestDTO));
    }

    @Test
    void updateUser_shouldThrowUserAlreadyExistsException_whenPasswordIsSame() {
        // Arrange
        when(usersRepository.findById(existingUser.getId())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(updateUserRequestDTO.getPassword(), existingUser.getPassword())).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> userService.updateUser(existingUser.getId(), updateUserRequestDTO));
    }
}
