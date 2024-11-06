package com.chess.tms.user_service.service;

import com.chess.tms.user_service.dto.UpdateUserRequestDTO;
import com.chess.tms.user_service.dto.UserResponseDTO;
import com.chess.tms.user_service.exception.UserAlreadyExistsException;
import com.chess.tms.user_service.exception.UserNotFoundException;
import com.chess.tms.user_service.model.User;
import com.chess.tms.user_service.repository.UsersRepository;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private UsersRepository usersRepository;

    private PasswordEncoder passwordEncoder;
    
    public UserService(UsersRepository usersRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDTO getUser(Long userId) {
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found."));
        return new UserResponseDTO(user.getUsername(), user.getEmail(), user.getRole());
    }

    public List<UserResponseDTO> getAllUsers() {
        List<User> users = usersRepository.findAll();
        return users.stream()
                .map(user -> new UserResponseDTO(user.getUsername(), user.getEmail(), user.getRole()))
                .collect(Collectors.toList());
    }

     public void updateUser(Long userId, UpdateUserRequestDTO updateUserRequestDTO) {
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found."));
        
        // Check if old password is correct
        if (updateUserRequestDTO.getOldPassword() != null && 
            !updateUserRequestDTO.getOldPassword().isEmpty()) {
            if (!passwordEncoder.matches(updateUserRequestDTO.getOldPassword(), user.getPassword())) {
               
                throw new UserNotFoundException("Old password is incorrect");
            }
        }

        // Check if email is the same or already exists
        if (updateUserRequestDTO.getEmail() != null && 
            !updateUserRequestDTO.getEmail().isEmpty()) {
            if (updateUserRequestDTO.getEmail().equals(user.getEmail())) {
               
                throw new UserAlreadyExistsException("Email is the same as the current email.");
            }
            if (usersRepository.findByEmail(updateUserRequestDTO.getEmail()).isPresent()) {
                throw new UserAlreadyExistsException("Email " + updateUserRequestDTO.getEmail() + " is already registered.");
            }
            user.setEmail(updateUserRequestDTO.getEmail());
        }

        // Check if the password is the same
        if (updateUserRequestDTO.getNewPassword() != null && 
            !updateUserRequestDTO.getNewPassword().isEmpty()) {
            if (passwordEncoder.matches(updateUserRequestDTO.getNewPassword(), user.getPassword())) {
                throw new UserAlreadyExistsException("Password is the same as the current password.");
            }
            user.setPassword(passwordEncoder.encode(updateUserRequestDTO.getNewPassword()));
        }

        // Update role if provided
        if (updateUserRequestDTO.getRole() != null) {
            user.setRole(updateUserRequestDTO.getRole());
        }

        user.setUpdatedAt(java.time.LocalDateTime.now());

        usersRepository.save(user);
    }
    

}