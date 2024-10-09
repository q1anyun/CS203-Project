package com.chess.tms.auth_service.controller;

import com.chess.tms.auth_service.dto.*;
import com.chess.tms.auth_service.security.JwtUtility;
import com.chess.tms.auth_service.service.AuthService;

import java.util.Map;

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

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody JwtRequest jwtRequest) throws Exception {
        AuthenticatedUserDTO authenticatedUser = authService.authenticate(jwtRequest);
        String jwtToken = jwtUtil.generateToken(authenticatedUser);
        JwtResponse jwtResponse = new JwtResponse();
        jwtResponse.setToken(jwtToken);
        jwtResponse.setExpiresIn(jwtUtil.getExpirationTime());
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setRole(authenticatedUser.getRole());
        loginResponse.setJwtResponse(jwtResponse);
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/register/player")
    public ResponseEntity<String> registerPlayer(@Valid @RequestBody PlayerRegistrationRequestDTO player) {
        return ResponseEntity.ok(authService.registerPlayer(player));
    }

    @PostMapping("/register/admin")
    public ResponseEntity<String> registerAdmin(@Valid @RequestBody AdminRegistrationRequestDTO admin) {
        return ResponseEntity.ok(authService.registerAdmin(admin));
    }
    
}