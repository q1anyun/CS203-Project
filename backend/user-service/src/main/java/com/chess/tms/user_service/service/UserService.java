package com.chess.tms.user_service.service;

import com.chess.tms.user_service.dto.UserRegistrationRequestDTO;
import com.chess.tms.user_service.dto.AdminRegistrationRequestDTO;
import com.chess.tms.user_service.dto.UserDetailsDTO;
import com.chess.tms.user_service.dto.UserRegistrationResponseDTO;
import com.chess.tms.user_service.exception.UserAlreadyExistsException;
import com.chess.tms.user_service.exception.UserNotFoundException;
import com.chess.tms.user_service.model.UserCredentials;
import com.chess.tms.user_service.model.UserDetails;
import com.chess.tms.user_service.repository.UserCredentialsRepository;
import com.chess.tms.user_service.repository.UserDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserCredentialsRepository userCredentialsRepository;

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    public static final int DEFAULT_ELO_RATING = 0;

    public UserRegistrationResponseDTO registerUser(UserRegistrationRequestDTO user) {

        // Check if username or email already exists
        if (userCredentialsRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists.");
        }
        if (userCredentialsRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists.");
        }

        // Map DTOs to entities
        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setUsername(user.getUsername());
        userCredentials.setEmail(user.getEmail());
        userCredentials.setPassword(user.getPassword());
        userCredentials.setRole(user.getRole());

        // Save UserCredentials
        UserCredentials savedCredentials = userCredentialsRepository.save(userCredentials);

        // Map and Save UserDetails
        UserDetails userDetails = new UserDetails();
        userDetails.setUserCredentials(savedCredentials);
        userDetails.setFirstName(user.getFirstName());
        userDetails.setLastName(user.getLastName());
        userDetails.setEloRating(DEFAULT_ELO_RATING);
        userDetails.setProfilePicture(user.getProfilePicture());

        UserDetails savedDetails = userDetailsRepository.save(userDetails);

        // Map saved entity to DTO and return
        UserRegistrationResponseDTO savedDetailsDTO = new UserRegistrationResponseDTO();
        savedDetailsDTO.setFirstName(savedDetails.getFirstName());
        savedDetailsDTO.setLastName(savedDetails.getLastName());

        return savedDetailsDTO;
    }

    public void registerAdmin(AdminRegistrationRequestDTO admin) {

        // Check if username or email already exists
        if (userCredentialsRepository.findByUsername(admin.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists.");
        }
        if (userCredentialsRepository.findByEmail(admin.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists.");
        }

        // Map DTOs to entities
        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setUsername(admin.getUsername());
        userCredentials.setEmail(admin.getEmail());
        userCredentials.setPassword(admin.getPassword());
        userCredentials.setRole(admin.getRole());

        // Save new Admin
        userCredentialsRepository.save(userCredentials);
    }

    public Optional<UserDetailsDTO> getUserDetailsByUserId(Long userId) {
        Optional<UserDetails> userDetails = userDetailsRepository.findByUserCredentialsId(userId);

        if (userDetails.isEmpty()) {
            throw new UserNotFoundException("User with ID " + userId + " not found.");
        }

        if (userDetails.isPresent()) {
            UserDetailsDTO userDetailsDTO = new UserDetailsDTO();
            userDetailsDTO.setId(userDetails.get().getId());
            userDetailsDTO.setUserId(userDetails.get().getUserCredentials().getId());
            userDetailsDTO.setFirstName(userDetails.get().getFirstName());
            userDetailsDTO.setLastName(userDetails.get().getLastName());
            userDetailsDTO.setEloRating(userDetails.get().getEloRating());
            userDetailsDTO.setProfilePicture(userDetails.get().getProfilePicture());
            return Optional.of(userDetailsDTO);
        }
        return Optional.empty();
    }
}