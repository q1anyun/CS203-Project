package com.chess.tms.user_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chess.tms.user_service.dto.AuthenticatedUserDTO;
import com.chess.tms.user_service.dto.JwtRequest;
import com.chess.tms.user_service.dto.JwtResponse;
import com.chess.tms.user_service.security.JwtUtility;
import com.chess.tms.user_service.service.UserService;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;

@RestController
@RequestMapping("/api/auth")
public class JwtController {

    private final JwtUtility jwtUtil;

    private final UserService userService;

    public JwtController(JwtUtility jwtUtil, UserService userService) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @PostMapping("/getPlayerId")
    public ResponseEntity<?> getCurrentUserPlayerId(@RequestHeader("Authorization") String tokenHeader) {
        try {
            // Extract token from the Authorization header
            String token = tokenHeader.startsWith("Bearer ") ? tokenHeader.substring(7) : tokenHeader;

            // Extract player ID from token
            Long playerId = jwtUtil.extractPlayerId(token);

            return ResponseEntity.ok(playerId);

        } catch (ExpiredJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("JWT token has expired.");
        } catch (UnsupportedJwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unsupported JWT token.");
        } catch (MalformedJwtException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Malformed JWT token.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or missing JWT claims.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing the token.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody JwtRequest jwtRequest) throws Exception {
        AuthenticatedUserDTO authenticatedUser = userService.authenticate(jwtRequest);
        String jwtToken = jwtUtil.generateToken(authenticatedUser);
        JwtResponse jwtResponse = new JwtResponse();
        jwtResponse.setToken(jwtToken);
        jwtResponse.setExpiresIn(jwtUtil.getExpirationTime());
        return ResponseEntity.ok(jwtResponse);
    }
}
