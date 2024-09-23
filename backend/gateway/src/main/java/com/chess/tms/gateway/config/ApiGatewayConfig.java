package com.chess.tms.gateway.config;

import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class ApiGatewayConfig {
    @Bean
    public RouterFunction<ServerResponse> playerServiceRoute(){
        return GatewayRouterFunctions.route("player-service")
                .route(RequestPredicates.path("/api/player/**"), HandlerFunctions.http("http://localhost:8081")).build();
    }
    
}
