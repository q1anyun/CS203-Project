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

    public AuthenticatedUserDTO authenticate(JwtRequest input) {
        User user = usersRepository.findByUsername(input.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getUsername(),
                        input.getPassword()));
        
        AuthenticatedUserDTO result = new AuthenticatedUserDTO();

        if (user.getRole() == UserRole.PLAYER) {
            PlayerDetails playerDetails = playerDetailsRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new UserNotFoundException("Player not found"));
            result.setPlayerId(playerDetails.getId());
        } else{
            result.setPlayerId(0L);
        }

        result.setEmail(user.getEmail());
        result.setRole(user.getRole());
        result.setUsername(user.getUsername());
        result.setUserId(user.getId());

        return result;

    }

    public String registerPlayer(PlayerRegistrationRequestDTO player) {
        if (usersRepository.findByUsername(player.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists.");
        }
        if (usersRepository.findByEmail(player.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists.");
        }

        User newUser = new User();
        newUser.setUsername(player.getUsername());
        newUser.setEmail(player.getEmail());
        newUser.setPassword(passwordEncoder.encode(player.getPassword()));
        newUser.setRole(UserRole.PLAYER);
        newUser.setCreatedAt(java.time.LocalDateTime.now());
        newUser.setUpdatedAt(java.time.LocalDateTime.now());

        User savedUser = usersRepository.save(newUser);

        PlayerDetails playerDetails = new PlayerDetails();
        playerDetails.setUserId(savedUser.getId());
        playerDetails.setFirstName(player.getFirstName());
        playerDetails.setLastName(player.getLastName());
        playerDetails.setProfilePicture(player.getProfilePicture());
        playerDetails.setEloRating(DEFAULT_ELO_RATING);
        playerDetails.setHighestElo(DEFAULT_ELO_RATING);
        playerDetails.setCountry(player.getCountry());

        playerDetailsRepository.save(playerDetails);

        return "Player created successfully";
    }

    public String registerAdmin(AdminRegistrationRequestDTO admin) {
        if (usersRepository.findByUsername(admin.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists.");
        }
        if (usersRepository.findByEmail(admin.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already exists.");
        }
        User newUser = new User();
        newUser.setUsername(admin.getUsername());
        newUser.setEmail(admin.getEmail());
        newUser.setPassword(passwordEncoder.encode(admin.getPassword()));
        newUser.setRole(UserRole.ADMIN);
        newUser.setCreatedAt(java.time.LocalDateTime.now());
        newUser.setUpdatedAt(java.time.LocalDateTime.now());

        usersRepository.save(newUser);
        return "Admin created successfully";
    }
}