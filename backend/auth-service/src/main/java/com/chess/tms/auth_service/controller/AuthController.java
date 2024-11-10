package com.chess.tms.auth_service.controller;

import com.chess.tms.auth_service.dto.*;
import com.chess.tms.auth_service.exception.UserNotFoundException;
import com.chess.tms.auth_service.security.JwtUtility;
import com.chess.tms.auth_service.service.AuthService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtility jwtUtil;

    public AuthController(AuthService authService, JwtUtility jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Authenticates a user and generates a JWT token upon successful login.
     *
     * @param jwtRequest the request object containing the user's credentials
     *                   (username and password)
     * @return a ResponseEntity containing a LoginResponse with the user's role,
     *         token, and expiration time
     * @throws UserNotFoundException if authentication fails due to incorrect
     *                               credentials or any
     */
    @PostMapping("/session")
    public ResponseEntity<LoginResponse> login(@RequestBody JwtRequest jwtRequest) throws Exception {
        // Authenticate the user and retrieve user details
        AuthenticatedUserDTO authenticatedUser = authService.authenticate(jwtRequest);

        // Generate a JWT token for the authenticated user
        String jwtToken = jwtUtil.generateToken(authenticatedUser);

        // Prepare the JWT response with the token and expiration time
        JwtResponse jwtResponse = new JwtResponse();
        jwtResponse.setToken(jwtToken);
        jwtResponse.setExpiresIn(jwtUtil.getExpirationTime());

        // Construct the final login response with user role and JWT information
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setRole(authenticatedUser.getRole());
        loginResponse.setJwtResponse(jwtResponse);

        return ResponseEntity.ok(loginResponse);
    }

    /**
     * Registers a new player with the provided registration details.
     *
     * @param player the DTO containing player registration information (validated)
     * @return a ResponseEntity containing a success message if registration is
     *         successful
     */
    @PostMapping("/users/player")
    public ResponseEntity<String> registerPlayer(@Valid @RequestBody PlayerRegistrationRequestDTO player) {
        return ResponseEntity.ok(authService.registerPlayer(player));
    }

    /**
     * Registers a new admin with the provided registration details.  
     *
     * @param admin the DTO containing admin registration information (validated)
     * @return a ResponseEntity containing a success message if registration is
     *         successful
     */
    @PostMapping("/users/admin")
    public ResponseEntity<String> registerAdmin(@Valid @RequestBody AdminRegistrationRequestDTO admin) {
        return ResponseEntity.ok(authService.registerAdmin(admin));
    }
}