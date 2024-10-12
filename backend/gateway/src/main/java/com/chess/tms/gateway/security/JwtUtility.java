package com.chess.tms.gateway.security;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;

import org.springframework.security.core.Authentication;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Component
public class JwtUtility {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.authorities.key}")
    public String authoritieskey;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Map<String, String> extractClaims(String token) {
        Map<String, String> claimsMap = new HashMap<>();

        Claims claims = extractAllClaims(token);

        // Debugging: Log the entire claims object
        System.out.println("Extracted claims: " + claims);

        // Safely extract claims and handle potential null values
        if (claims.get("userId") != null) {
            claimsMap.put("userId", String.valueOf(claims.get("userId", Long.class)));
            System.out.println("Extracted userId: " + claimsMap.get("userId"));
        } else {
            System.out.println("userId claim is missing.");
        }

        if (claims.get("role") != null) {
            claimsMap.put("role", claims.get("role", String.class));
            System.out.println("Extracted role: " + claimsMap.get("role"));
        } else {
            System.out.println("role claim is missing.");
        }

        if (claims.get("playerId") != null) {
            claimsMap.put("playerId", String.valueOf(claims.get("playerId", Long.class)));
            System.out.println("Extracted playerId: " + claimsMap.get("playerId"));
        } else {
            System.out.println("playerId claim is missing.");
        }

        return claimsMap;
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public long getExpirationTime() {
        return expiration;
    }

    // private String createToken(Map<String, Object> claims, String subject) {
    // return Jwts.builder()
    // .setClaims(claims)
    // .setSubject(subject)
    // .setIssuedAt(new Date(System.currentTimeMillis()))
    // .setExpiration(new Date(System.currentTimeMillis() + expiration))
    // .signWith(getSigningKey(), SignatureAlgorithm.HS256)
    // .compact();
    // }

    public String generateToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim(authoritieskey, authorities)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public ResponseEntity<?> validateToken(String token) {

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("userId", claims.get("userId"));
            response.put("role", claims.get("role"));
            return ResponseEntity.ok(response);
        } catch (JwtException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
        }
    }

}
