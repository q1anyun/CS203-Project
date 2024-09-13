package com.chess.tms.user_service.service;

import org.springframework.web.client.RestTemplate;

import com.chess.tms.user_service.dto.AdminRegistrationRequestDTO;
import com.chess.tms.user_service.dto.PlayerRegistrationDTO;
import com.chess.tms.user_service.dto.PlayerRegistrationRequestDTO;
import com.chess.tms.user_service.dto.PlayerRegistrationResponseDTO;
import com.chess.tms.user_service.enums.UserRole;
import com.chess.tms.user_service.exception.UserAlreadyExistsException;
import com.chess.tms.user_service.model.Users;
import com.chess.tms.user_service.repository.UsersRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Value;

@Service
public class UserService {

    @Autowired
    private UsersRepository usersRepository;
    private final RestTemplate restTemplate;

    @Value("${player.service.url}")
    private String playerServiceUrl;

    public UserService(RestTemplate restTemplate, UsersRepository userCredentialsRepository) {
        this.restTemplate = restTemplate;
        this.usersRepository = userCredentialsRepository;
    }

    // @Autowired
    // private PlayerDetailsRepository userDetailsRepository;

    // @Override
    // public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    //     // Fetch user credentials from database
    //     UserCredentials userCredentials = userCredentialsRepository.findByUsername(username)
    //             .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

    //     // Map UserCredentials to Spring Security's UserDetails
    //     return User.builder()
    //             .username(userCredentials.getUsername())
    //             .password(userCredentials.getPassword())  // Ensure password is already encoded (e.g., BCrypt)
    //             .roles(userCredentials.getRole().toString())  // Map roles (USER, ADMIN, etc.)
    //             .build();
    // }

    public PlayerRegistrationResponseDTO registerUser(PlayerRegistrationRequestDTO user) {

        // Check if username or email already exists
        if (usersRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists.");
        }
        if (usersRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists.");
        }

        // Map DTOs to entities
        Users newUser = new Users();
        newUser.setUsername(user.getUsername());
        newUser.setEmail(user.getEmail());
        newUser.setPassword(user.getPassword());
        newUser.setRole(UserRole.PLAYER);

        // Save User
        Users savedUser = usersRepository.save(newUser);

        // Create PlayerDetails in Player Service
        PlayerRegistrationDTO playerRegistrationDTO = new PlayerRegistrationDTO();
        playerRegistrationDTO.setUserId(savedUser.getId());
        playerRegistrationDTO.setFirstName(user.getFirstName());
        playerRegistrationDTO.setLastName(user.getLastName());
        playerRegistrationDTO.setProfilePicture(user.getProfilePicture());

        String registerEndpoint = playerServiceUrl + "/register";

        return restTemplate.postForObject(registerEndpoint, playerRegistrationDTO, PlayerRegistrationResponseDTO.class);
    }

    public void registerAdmin(AdminRegistrationRequestDTO admin) {

        // Check if username or email already exists
        if (usersRepository.findByUsername(admin.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists.");
        }
        if (usersRepository.findByEmail(admin.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists.");
        }

        // Map DTOs to entities
        Users userCredentials = new Users();
        userCredentials.setUsername(admin.getUsername());
        userCredentials.setEmail(admin.getEmail());
        userCredentials.setPassword(admin.getPassword());
        userCredentials.setRole(admin.getRole());

        // Save new Admin
        usersRepository.save(userCredentials);
    }

    // public Optional<PlayerDetailsDTO> getUserDetailsByUserId(Long userId) {
    //     Optional<PlayerDetails> userDetails = userDetailsRepository.findByUserCredentialsId(userId);

    //     if (userDetails.isEmpty()) {
    //         throw new UserNotFoundException("User with ID " + userId + " not found.");
    //     }

    //     if (userDetails.isPresent()) {
    //         PlayerDetailsDTO userDetailsDTO = new PlayerDetailsDTO();
    //         userDetailsDTO.setId(userDetails.get().getId());
    //         userDetailsDTO.setUserId(userDetails.get().getUserCredentials().getId());
    //         userDetailsDTO.setFirstName(userDetails.get().getFirstName());
    //         userDetailsDTO.setLastName(userDetails.get().getLastName());
    //         userDetailsDTO.setEloRating(userDetails.get().getEloRating());
    //         userDetailsDTO.setProfilePicture(userDetails.get().getProfilePicture());
    //         return Optional.of(userDetailsDTO);
    //     }
    //     return Optional.empty();
    // }
}