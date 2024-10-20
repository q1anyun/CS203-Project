package com.chess.tms.user_service.controller;

import com.chess.tms.user_service.dto.UpdateUserRequestDTO;
import com.chess.tms.user_service.dto.UserResponseDTO;
import com.chess.tms.user_service.service.UserService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/current")
    public ResponseEntity<UserResponseDTO> getUser(@RequestHeader("X-User-Id") String id) {
        Long userId = Long.parseLong(id);
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @GetMapping("")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/current")
    public ResponseEntity<String> updateUser(
            @RequestHeader("X-User-Id") String id,
            @Valid @RequestBody UpdateUserRequestDTO updateUserRequestDTO) {
        Long userId = Long.parseLong(id);
        userService.updateUser(userId, updateUserRequestDTO);
        return ResponseEntity.ok("Successfully updated credentials");
    }
}