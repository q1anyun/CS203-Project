package com.chess.tms.player_service.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

@Service
public class TokenValidationService {

    private final RestTemplate restTemplate;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    public TokenValidationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean validateToken(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            System.out.println("Token: " + token);
            headers.set("Authorization", token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(authServiceUrl + "/api/auth/validate", entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Boolean valid = (Boolean) response.getBody().get("valid");
                return valid != null && valid;
            }
        } catch (HttpClientErrorException e) {
            // Handle 401 Unauthorized or other client errors
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                System.out.println("Token is invalid or expired: " + e.getMessage());
            } else {
                System.out.println("Client error occurred: " + e.getMessage());
            }
        } catch (HttpServerErrorException e) {
            // Handle 5xx server errors from the authentication service
            System.out.println("Auth service is down or internal error occurred: " + e.getMessage());
        } catch (Exception e) {
            // Handle any other unforeseen errors
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }
        
        return false;
    }
}
