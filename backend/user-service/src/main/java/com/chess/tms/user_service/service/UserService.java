package com.chess.tms.user_service.service;

import com.chess.tms.user_service.dto.*;
import com.chess.tms.user_service.enums.UserRole;
import com.chess.tms.user_service.exception.UserAlreadyExistsException;
import com.chess.tms.user_service.exception.UserNotFoundException;
import com.chess.tms.user_service.model.User;
import com.chess.tms.user_service.repository.UsersRepository;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    public UserService(RestTemplate restTemplate, UsersRepository usersRepository,
            AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.restTemplate = restTemplate;
        this.usersRepository = usersRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthenticatedUserDTO authenticate(JwtRequest input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getUsername(),
                        input.getPassword()));
        User user = usersRepository.findByUsername(input.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String getPlayerDetailsEndpoint = playerServiceUrl + "/" + user.getId() + "/user";

        AuthenticatedUserDTO result = new AuthenticatedUserDTO();
        result.setEmail(user.getEmail());
        result.setPlayerId(restTemplate.getForObject(getPlayerDetailsEndpoint, Long.class));
        result.setRole(user.getRole());
        result.setUsername(user.getUsername());
        result.setUserId(user.getId());

        return result;

    }

    public String registerPlayer(PlayerRegistrationRequestDTO player,String tokenHeader) {
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

        PlayerRegistrationDTO playerRegistrationDTO = new PlayerRegistrationDTO();
        playerRegistrationDTO.setUserId(savedUser.getId());
        playerRegistrationDTO.setFirstName(player.getFirstName());
        playerRegistrationDTO.setLastName(player.getLastName());
        playerRegistrationDTO.setProfilePicture(player.getProfilePicture());

        String registerEndpoint = playerServiceUrl + "/register";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + tokenHeader);
        HttpEntity<Object> entity = new HttpEntity<>(playerRegistrationDTO, headers);

        return restTemplate.postForObject(registerEndpoint, entity, String.class);
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