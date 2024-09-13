package com.chess.tms.user_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chess.tms.user_service.dto.AdminRegistrationRequestDTO;
import com.chess.tms.user_service.dto.PlayerRegistrationRequestDTO;
import com.chess.tms.user_service.dto.PlayerRegistrationResponseDTO;
import com.chess.tms.user_service.service.UserService;


@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register/user")
    public ResponseEntity<PlayerRegistrationResponseDTO> registerUser(@RequestBody PlayerRegistrationRequestDTO user) {
        return ResponseEntity.ok(userService.registerUser(user));
    }

    @PostMapping("/register/admin")
    public ResponseEntity<String> registerAdmin(@RequestBody AdminRegistrationRequestDTO admin) {
        return ResponseEntity.ok("Admin successfully created");
    }
}
