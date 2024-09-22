package com.chess.tms.user_service.controller;

import com.chess.tms.user_service.dto.*;
import com.chess.tms.user_service.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register/player")
    public ResponseEntity<String> registerPlayer(@RequestBody PlayerRegistrationRequestDTO player) {
        return ResponseEntity.ok(userService.registerPlayer(player));
    }

    @PostMapping("/register/admin")
    public ResponseEntity<String> registerAdmin(@RequestBody AdminRegistrationRequestDTO admin) {
        return ResponseEntity.ok(userService.registerAdmin(admin));
    }
}