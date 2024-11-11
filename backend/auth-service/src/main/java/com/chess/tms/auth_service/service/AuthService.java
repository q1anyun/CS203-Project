package com.chess.tms.auth_service.service;

import com.chess.tms.auth_service.dto.*;
import com.chess.tms.auth_service.enums.UserRole;
import com.chess.tms.auth_service.exception.*;
import com.chess.tms.auth_service.model.*;
import com.chess.tms.auth_service.repository.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsersRepository usersRepository;
    private final PlayerDetailsRepository playerDetailsRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public static final int DEFAULT_ELO_RATING = 500;

    public AuthService(PlayerDetailsRepository playerDetailsRepository, UsersRepository usersRepository,
            AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.playerDetailsRepository = playerDetailsRepository;
    }

    /**
     * Authenticates a user using the provided credentials, retrieves user details,
     * and constructs
     * an AuthenticatedUserDTO containing the authenticated user's information.
     *
     * @param input the JwtRequest containing the user's username and password
     * @return an AuthenticatedUserDTO with the user's ID, username, email, role,
     *         and player ID (if applicable)
     * @throws UserNotFoundException if the user is not found or if a
     *                               player-specific detail is missing for a PLAYER
     *                               role
     */
    public AuthenticatedUserDTO authenticate(JwtRequest input) {
        // Retrieve user from the repository or throw exception if not found
        User user = usersRepository.findByUsername(input.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Perform authentication with username and password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getUsername(),
                        input.getPassword()));

        // Initialize DTO for authenticated user details
        AuthenticatedUserDTO result = new AuthenticatedUserDTO();

        // Set player-specific details if the user role is PLAYER
        if (user.getRole() == UserRole.PLAYER) {
            PlayerDetails playerDetails = playerDetailsRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new UserNotFoundException("Player not found"));
            result.setPlayerId(playerDetails.getId());
        } else {
            result.setPlayerId(0L);
        }

        // Populate DTO with common user details
        result.setEmail(user.getEmail());
        result.setRole(user.getRole());
        result.setUsername(user.getUsername());
        result.setUserId(user.getId());

        return result;

    }

    /**
     * Registers a new player by validating the provided username and email,
     * creating a new user,
     * and saving player-specific details.
     *
     * @param player the DTO containing the player's registration information
     * @return a success message indicating that the player was created successfully
     * @throws UserAlreadyExistsException if the username or email already exists in
     *                                    the system
     */
    public String registerPlayer(PlayerRegistrationRequestDTO player) {
        // Check if the username already exists in the system
        if (usersRepository.findByUsername(player.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists.");
        }

        // Check if the email already exists in the system
        if (usersRepository.findByEmail(player.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists.");
        }

        // Create a new user object and set the player's details
        User newUser = new User();
        newUser.setUsername(player.getUsername());
        newUser.setEmail(player.getEmail());
        newUser.setPassword(passwordEncoder.encode(player.getPassword()));
        newUser.setRole(UserRole.PLAYER);
        newUser.setCreatedAt(java.time.LocalDateTime.now());
        newUser.setUpdatedAt(java.time.LocalDateTime.now());

        // Save the newly created user to the database
        User savedUser = usersRepository.save(newUser);

        // Create and populate the player's details object
        PlayerDetails playerDetails = new PlayerDetails();
        playerDetails.setUserId(savedUser.getId());
        playerDetails.setFirstName(player.getFirstName());
        playerDetails.setLastName(player.getLastName());
        playerDetails.setProfilePicture(player.getProfilePicture());
        playerDetails.setEloRating(DEFAULT_ELO_RATING);
        playerDetails.setHighestElo(DEFAULT_ELO_RATING);
        playerDetails.setCountry(player.getCountry());

        // Save the player's details to the database
        playerDetailsRepository.save(playerDetails);

        return "Player created successfully";
    }

    /**
     * Registers a new admin by validating the provided username and email, creating
     * a new admin user,
     * and saving the user to the database.
     *
     * @param admin the DTO containing the admin's registration information
     * @return a success message indicating that the admin was created successfully
     * @throws UserAlreadyExistsException if the username or email already exists in
     *                                    the system
     */
    public String registerAdmin(AdminRegistrationRequestDTO admin) {
        // Check if the username already exists in the system
        if (usersRepository.findByUsername(admin.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists.");
        }

        // Check if the email already exists in the system
        if (usersRepository.findByEmail(admin.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists.");
        }

        // Create a new user object and set the admin's details
        User newUser = new User();
        newUser.setUsername(admin.getUsername());
        newUser.setEmail(admin.getEmail());
        newUser.setPassword(passwordEncoder.encode(admin.getPassword()));
        newUser.setRole(UserRole.ADMIN);
        newUser.setCreatedAt(java.time.LocalDateTime.now());
        newUser.setUpdatedAt(java.time.LocalDateTime.now());

        // Save the newly created admin user to the database
        usersRepository.save(newUser);
        
        return "Admin created successfully";
    }
}