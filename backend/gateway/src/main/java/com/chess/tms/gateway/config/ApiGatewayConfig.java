package com.chess.tms.gateway.config;

import org.springframework.http.HttpHeaders;
import java.util.Map;

import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import org.springframework.beans.factory.annotation.Value;

import com.chess.tms.gateway.security.JwtUtility;

@Configuration
public class ApiGatewayConfig {

    // JWT utility for token processing
    private final JwtUtility jwtUtility;
    
    // URLs for microservices
    @Value("${auth.service.url}")
    private String authServiceUrl;
    
    @Value("${users.service.url}")
    private String usersServiceUrl;
    
    @Value("${players.service.url}")
    private String playersServiceUrl;
    
    @Value("${tournaments.service.url}")
    private String tournamentsServiceUrl;
    
    @Value("${matches.service.url}")
    private String matchesServiceUrl;
    
    @Value("${elo.service.url}")
    private String eloServiceUrl;
    
    @Value("${s3.upload.service.url}")
    private String s3UploadServiceUrl;

    // Constructor for dependency injection
    public ApiGatewayConfig(JwtUtility jwtUtility) {
        this.jwtUtility = jwtUtility;
    }

    // Authentication service routes
    @Bean
    public RouterFunction<ServerResponse> authServiceRoute() {
        return GatewayRouterFunctions.route("auth-service")
                .route(RequestPredicates.path("/api/auth/**"), HandlerFunctions.http(authServiceUrl)).build()
                .andRoute(RequestPredicates.path("/api/otp/**"), HandlerFunctions.http(authServiceUrl));
    }
    
    // User service routes
    @Bean
    public RouterFunction<ServerResponse> userServiceRoute() {
        return GatewayRouterFunctions.route("user-service")
                .route(RequestPredicates.path("/api/user/**"), request -> 
                    processRequestWithJwtClaims(request, usersServiceUrl))
                .build();
    }

    // Player service routes
    @Bean
    public RouterFunction<ServerResponse> playerServiceRoute() {
        return GatewayRouterFunctions.route("player-service")
                .route(RequestPredicates.path("/api/player/**"), request -> 
                    processRequestWithJwtClaims(request, playersServiceUrl))
                .build();
    }

    // Tournament service routes
    @Bean
    public RouterFunction<ServerResponse> tournamentServiceRoute() {
        return GatewayRouterFunctions
            .route(RequestPredicates.path("/api/tournaments/**"), 
                request -> processRequestWithJwtClaims(request, tournamentsServiceUrl))
            .andRoute(RequestPredicates.path("/api/tournament-players/**"), 
                request -> processRequestWithJwtClaims(request, tournamentsServiceUrl))
            .andRoute(RequestPredicates.path("/api/round-type/**"), 
                request -> processRequestWithJwtClaims(request, tournamentsServiceUrl))
            .andRoute(RequestPredicates.path("/api/game-type/**"), 
                request -> processRequestWithJwtClaims(request, tournamentsServiceUrl))
            .andRoute(RequestPredicates.path("/api/tournament-type/**"), 
                request -> processRequestWithJwtClaims(request, tournamentsServiceUrl))
            .andRoute(RequestPredicates.path("/api/swiss-bracket/**"), 
                request -> processRequestWithJwtClaims(request, tournamentsServiceUrl))
            .andRoute(RequestPredicates.path("/api/swiss-standing/**"), 
                request -> processRequestWithJwtClaims(request, tournamentsServiceUrl));
    }

    // Match service routes
    @Bean
    public RouterFunction<ServerResponse> matchServiceRoute() {
        return GatewayRouterFunctions.route("match-service")
                .route(RequestPredicates.path("/api/matches/**"), request -> 
                    processRequestWithJwtClaims(request, matchesServiceUrl))
                .build();
    }

    // Elo service routes
    @Bean
    public RouterFunction<ServerResponse> eloServiceRoute() {
        return GatewayRouterFunctions.route("elo-service")
                .route(RequestPredicates.path("/api/elo/**"), request ->
                    processRequestWithJwtClaims(request, eloServiceUrl))
                .build();
    }
    
    // S3 upload service routes (for file uploads)
    @Bean
    public RouterFunction<ServerResponse> s3UploadServiceRoute() {
        return GatewayRouterFunctions.route("s3-upload-service")
                .route(RequestPredicates.path("/api/s3/**"), request ->
                    processRequestWithJwtClaims(request, s3UploadServiceUrl))
                .build();
    }

    // Helper method to process requests with JWT claims
    private ServerResponse processRequestWithJwtClaims(ServerRequest request, String forwardUri) {
        // Extract the Authorization header
        String authHeader = request.headers().firstHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
    
            // Extract claims from JWT
            Map<String, String> claims = jwtUtility.extractClaims(token);
    
            // Add claims to headers for downstream services
            ServerRequest modifiedRequest = ServerRequest.from(request)
                    .header("X-User-Id", claims.get("userId"))
                    .header("X-User-Role", claims.get("role"))
                    .header("X-User-PlayerId", claims.get("playerId"))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .build();
    
            // Forward the modified request to the target service
            try {
                return HandlerFunctions.http(forwardUri).handle(modifiedRequest);
            } catch (Exception e) {
                return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
        // If Authorization header is missing or invalid, just forward without headers
        try {
            return HandlerFunctions.http(forwardUri).handle(request);
        } catch (Exception e) {
            System.out.println("Error forwarding request: " + e.getMessage());
            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
