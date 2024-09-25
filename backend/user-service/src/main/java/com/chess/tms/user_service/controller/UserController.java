package com.chess.tms.user_service.controller;

import com.chess.tms.user_service.dto.UpdateUserRequestDTO;
import com.chess.tms.user_service.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Update User Endpoint
    @PutMapping("/{userId}")
    public ResponseEntity<String> updateUser(
            @PathVariable Long userId,
            @RequestBody UpdateUserRequestDTO updateUserRequestDTO) {
        
        userService.updateUser(userId, updateUserRequestDTO);
        return ResponseEntity.ok("Successfully updated credentials");
    }
}