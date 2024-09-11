package com.chess.tms.user_service.controller;

import com.chess.tms.user_service.model.UserCredentials;
import com.chess.tms.user_service.model.UserDetails;
import com.chess.tms.user_service.service.UserService;
import com.chess.tms.user_service.dto.UserRegistrationRequestDTO;
import com.chess.tms.user_service.dto.AdminRegistrationRequestDTO;
import com.chess.tms.user_service.dto.UserDetailsDTO;
import com.chess.tms.user_service.dto.UserRegistrationResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register/user")
    public ResponseEntity<UserRegistrationResponseDTO> registerUser(@RequestBody UserRegistrationRequestDTO user) {
        return ResponseEntity.ok(userService.registerUser(user));
    }

    @PostMapping("/register/admin")
    public ResponseEntity<String> registerAdmin(@RequestBody AdminRegistrationRequestDTO admin) {
        return ResponseEntity.ok("Admin successfully created");
    }

    @GetMapping("/{userId}/details")
    public ResponseEntity<UserDetailsDTO> getUserDetails(@PathVariable("userId") Long userId) {
        Optional<UserDetailsDTO> userDetails = userService.getUserDetailsByUserId(userId);
        return userDetails.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
