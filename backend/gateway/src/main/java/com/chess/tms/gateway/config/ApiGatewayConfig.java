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
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import com.chess.tms.gateway.security.JwtUtility;

@Configuration
public class ApiGatewayConfig {

    private final JwtUtility jwtUtility;

    public ApiGatewayConfig(JwtUtility jwtUtility) {
        this.jwtUtility = jwtUtility;
    }

    @Bean
    public RouterFunction<ServerResponse> authServiceRoute() {
        return GatewayRouterFunctions.route("auth-service")
                .route(RequestPredicates.path("/api/auth/**"), HandlerFunctions.http("http://localhost:8081")).build();
    }
    
    @Bean
    public RouterFunction<ServerResponse> userServiceRoute() {
        return GatewayRouterFunctions.route("user-service")
                .route(RequestPredicates.path("/api/user/**"), request -> 
                    processRequestWithJwtClaims(request, "http://localhost:8082"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> playerServiceRoute() {
        return GatewayRouterFunctions.route("player-service")
                .route(RequestPredicates.path("/api/player/**"), request -> 
                    processRequestWithJwtClaims(request, "http://localhost:8083"))
                .build();
    }

    @Bean
public RouterFunction<ServerResponse> tournamentServiceRoute() {
    return RouterFunctions
        .route(RequestPredicates.path("/api/tournaments/**"), 
            request -> processRequestWithJwtClaims(request, "http://localhost:8084"))
        .andRoute(RequestPredicates.path("/api/tournamentplayers/**"), 
            request -> processRequestWithJwtClaims(request, "http://localhost:8084"));
}

    @Bean
    public RouterFunction<ServerResponse> matchServiceRoute() {
        return GatewayRouterFunctions.route("match-service")
                .route(RequestPredicates.path("/api/matches/**"), request -> 
                    processRequestWithJwtClaims(request, "http://localhost:8085"))
                .build();
    }

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
