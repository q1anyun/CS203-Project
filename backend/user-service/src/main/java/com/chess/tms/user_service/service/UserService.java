package com.chess.tms.user_service.service;

import com.chess.tms.user_service.dto.*;
import com.chess.tms.user_service.enums.UserRole;
import com.chess.tms.user_service.exception.UserAlreadyExistsException;
import com.chess.tms.user_service.model.User;
import com.chess.tms.user_service.repository.UsersRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;
    private final AuthenticationManager authenticationManager;

    @Value("${player.service.url}")
    private String playerServiceUrl;

    public UserService(RestTemplate restTemplate, UsersRepository usersRepository, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.restTemplate = restTemplate;
        this.usersRepository = usersRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    public User authenticate(JwtRequest input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getUsername(),
                        input.getPassword()
                )
        );
        return usersRepository.findByUsername(input.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
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

        User savedUser = usersRepository.save(newUser);

        PlayerRegistrationDTO playerRegistrationDTO = new PlayerRegistrationDTO();
        playerRegistrationDTO.setUserId(savedUser.getId());
        playerRegistrationDTO.setFirstName(player.getFirstName());
        playerRegistrationDTO.setLastName(player.getLastName());
        playerRegistrationDTO.setProfilePicture(player.getProfilePicture());

        String registerEndpoint = playerServiceUrl + "/register";

        return restTemplate.postForObject(registerEndpoint, playerRegistrationDTO, String.class);
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
        newUser.setRole(admin.getRole());

        usersRepository.save(newUser);
        return "Admin created successfully";
    }
}