package com.chess.tms.gateway.config;

import org.springframework.http.HttpHeaders;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import com.chess.tms.gateway.security.JwtUtility;

@Configuration
public class ApiGatewayConfig {

    private final JwtUtility jwtUtility;

    @Value("${tournaments.service.url}")
    private String tournamentServiceUrl;

    @Value("${elo.service.url}")
    private String eloServiceUrl;

    @Value("${players.service.url}")
    private String playerServiceUrl;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    @Value("${users.service.url}")
    private String usersServiceUrl;

    @Value("${matches.service.url}")
    private String matchesServiceUrl;

    @Value("${s3.upload.service.url}")
    private String s3UploadServiceUrl;

    public ApiGatewayConfig(JwtUtility jwtUtility) {
        this.jwtUtility = jwtUtility;
    }

    @Bean
    public RouterFunction<ServerResponse> authServiceRoute() {
        return GatewayRouterFunctions.route("auth-service")
                .route(RequestPredicates.path("/api/auth/**"), HandlerFunctions.http(authServiceUrl)).build()
                .andRoute(RequestPredicates.path("/api/otp/**"), HandlerFunctions.http(authServiceUrl));
    }
    
    @Bean
    public RouterFunction<ServerResponse> userServiceRoute() {
        return GatewayRouterFunctions.route("user-service")
                .route(RequestPredicates.path("/api/user/**"), request -> 
                    processRequestWithJwtClaims(request, usersServiceUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> playerServiceRoute() {
        return GatewayRouterFunctions.route("player-service")
                .route(RequestPredicates.path("/api/player/**"), request -> 
                    processRequestWithJwtClaims(request, playerServiceUrl))
                .build();
    }

    @Bean
public RouterFunction<ServerResponse> tournamentServiceRoute() {
    return GatewayRouterFunctions
        .route(RequestPredicates.path("/api/tournaments/**"), 
            request -> processRequestWithJwtClaims(request, tournamentServiceUrl))
        .andRoute(RequestPredicates.path("/api/tournament-players/**"), 
            request -> processRequestWithJwtClaims(request, tournamentServiceUrl))
            .andRoute(RequestPredicates.path("/api/round-type/**"), 
            request -> processRequestWithJwtClaims(request, tournamentServiceUrl))
            .andRoute(RequestPredicates.path("/api/game-type/**"), 
            request -> processRequestWithJwtClaims(request, tournamentServiceUrl))
            .andRoute(RequestPredicates.path("/api/tournament-type/**"), 
            request -> processRequestWithJwtClaims(request, tournamentServiceUrl))
            .andRoute(RequestPredicates.path("/api/swiss-bracket/**"), 
            request -> processRequestWithJwtClaims(request, tournamentServiceUrl))
            .andRoute(RequestPredicates.path("/api/swiss-standing/**"), 
            request -> processRequestWithJwtClaims(request, tournamentServiceUrl));
}
    @Bean
    public RouterFunction<ServerResponse> matchServiceRoute() {
        return GatewayRouterFunctions.route("match-service")
                .route(RequestPredicates.path("/api/matches/**"), request -> 
                    processRequestWithJwtClaims(request, matchesServiceUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> eloServiceRoute() {
        return GatewayRouterFunctions.route("elo-service")
                .route(RequestPredicates.path("/api/elo/**"), request ->
                    processRequestWithJwtClaims(request, eloServiceUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> s3UploadServiceRoute() {
        return GatewayRouterFunctions.route("s3-upload-service")
                .route(RequestPredicates.path("/api/s3/**"), request ->
                    processRequestWithJwtClaims(request, s3UploadServiceUrl))
                .build();
    }

    private ServerResponse processRequestWithJwtClaims(ServerRequest request, String forwardUri) {
        System.out.println("Running processRequestWithJwtClaims");
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
    
            try {
                return HandlerFunctions.http(forwardUri).handle(modifiedRequest);
            } catch (Exception e) {
                return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
        // If Authorization header is missing or invalid, just forward without headers
        try {
            System.out.println("No token found, forwarding without headers");
            return HandlerFunctions.http(forwardUri).handle(request);
        } catch (Exception e) {
            System.out.println("Error forwarding request: " + e.getMessage());
            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
