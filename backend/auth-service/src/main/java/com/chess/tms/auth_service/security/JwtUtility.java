package com.chess.tms.auth_service.security;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.chess.tms.auth_service.dto.AuthenticatedUserDTO;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Component
public class JwtUtility {
    /**
     * Secret key used for JWT signing, injected from application properties
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Token expiration time in milliseconds, injected from application properties
     */
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * Creates a SecretKey instance from the JWT secret string
     * @return SecretKey for JWT signing
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    /**
     * Extracts the username from the JWT token
     * @param token JWT token string
     * @return username stored in the token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Generic method to extract any claim from the token
     * @param token JWT token string
     * @param claimsResolver Function to extract specific claim
     * @return Extracted claim value of type T
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extracts all claims from the token
     * @param token JWT token string
     * @return Claims object containing all token claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Checks if the token has expired
     * @param token JWT token string
     * @return true if token is expired, false otherwise
     */
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date from the token
     * @param token JWT token string
     * @return Date when the token expires
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generates a JWT token for an authenticated user
     * @param userDetails DTO containing authenticated user information
     * @return JWT token string containing user claims (role, userId, playerId)
     */
    public String generateToken(AuthenticatedUserDTO userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // Add user details to the token claims - used for processing requests
        claims.put("role", userDetails.getRole());
        claims.put("userId", userDetails.getUserId());
        claims.put("playerId", userDetails.getPlayerId());
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Returns the configured token expiration time
     * @return expiration time in milliseconds
     */
    public long getExpirationTime() {
        return expiration;
    }

    /**
     * Creates a JWT token with the specified claims and subject
     * @param claims Map of claims to include in the token
     * @param subject The subject (typically username) of the token
     * @return Signed JWT token string
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates if the token is valid for the given user
     * @param token JWT token string to validate
     * @param userDetails UserDetails object to validate against
     * @return true if token is valid for the user and not expired, false otherwise
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
