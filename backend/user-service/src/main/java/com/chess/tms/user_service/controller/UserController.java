package com.chess.tms.user_service.controller;

import com.chess.tms.user_service.dto.*;
import com.chess.tms.user_service.model.User;
import com.chess.tms.user_service.security.JwtUtility;
import com.chess.tms.user_service.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final JwtUtility jwtUtil;
    private final UserService userService;

    public UserController(JwtUtility jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @PostMapping("/register/user")
    public ResponseEntity<String> registerUser(@RequestBody PlayerRegistrationRequestDTO player) {
        return ResponseEntity.ok(userService.registerPlayer(player));
    }

    @PostMapping("/register/admin")
    public ResponseEntity<String> registerAdmin(@RequestBody AdminRegistrationRequestDTO admin) {
        return ResponseEntity.ok(userService.registerAdmin(admin));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody JwtRequest jwtRequest) throws Exception {
        User authenticatedUser = userService.authenticate(jwtRequest);
        String jwtToken = jwtUtil.generateToken(authenticatedUser);
        JwtResponse jwtResponse = new JwtResponse();
        jwtResponse.setToken(jwtToken);
        jwtResponse.setExpiresIn(jwtUtil.getExpirationTime());
        return ResponseEntity.ok(jwtResponse);
    }
}