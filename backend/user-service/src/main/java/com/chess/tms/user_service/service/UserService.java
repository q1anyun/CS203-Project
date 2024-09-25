package com.chess.tms.user_service.service;

import com.chess.tms.user_service.dto.UpdateUserRequestDTO;
import com.chess.tms.user_service.exception.UserAlreadyExistsException;
import com.chess.tms.user_service.exception.UserNotFoundException;
import com.chess.tms.user_service.model.User;
import com.chess.tms.user_service.repository.UsersRepository;
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

     public void updateUser(Long userId, UpdateUserRequestDTO updateUserRequestDTO) {
        User user = usersRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found."));

        // Check if username is the same or already exists
        if (updateUserRequestDTO.getUsername() != null && 
            !updateUserRequestDTO.getUsername().isEmpty()) {
            if (updateUserRequestDTO.getUsername().equals(user.getUsername())) {
                throw new UserAlreadyExistsException("Username is the same as the current username.");
            }
            if (usersRepository.findByUsername(updateUserRequestDTO.getUsername()).isPresent()) {
                throw new UserAlreadyExistsException("Username " + updateUserRequestDTO.getUsername() + " is already taken.");
            }
            user.setUsername(updateUserRequestDTO.getUsername());
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
        if (updateUserRequestDTO.getPassword() != null && 
            !updateUserRequestDTO.getPassword().isEmpty()) {
            if (passwordEncoder.matches(updateUserRequestDTO.getPassword(), user.getPassword())) {
                throw new UserAlreadyExistsException("Password is the same as the current password.");
            }
            user.setPassword(passwordEncoder.encode(updateUserRequestDTO.getPassword()));
        }

        // Update role if provided
        if (updateUserRequestDTO.getRole() != null) {
            user.setRole(updateUserRequestDTO.getRole());
        }

        user.setUpdatedAt(java.time.LocalDateTime.now());

        usersRepository.save(user);
    }
    

}