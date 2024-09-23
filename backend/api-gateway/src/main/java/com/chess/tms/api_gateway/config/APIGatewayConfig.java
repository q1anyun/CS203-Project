package com.chess.tms.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.chess.tms.api_gateway.security.JwtAuthenticationFilter;

import io.netty.resolver.DefaultAddressResolverGroup;
import reactor.netty.http.client.HttpClient;

@Configuration
public class ApiGatewayConfig {
    private final JwtAuthenticationFilter filter;

    public ApiGatewayConfig(JwtAuthenticationFilter filter) {
        this.filter = filter;
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("player-service", r -> r.path("/api/player/**")
                        .filters(f -> f.filter(filter))
                        .uri("lb://player-service")) // Load Balance: use Eureka to locate instances and LB

                // .route("job-service", r -> r.path("/v1/job-service/**")
                //         .filters(f -> f.filter(filter))
                //         .uri("lb://job-service"))

                // .route("notification-service", r -> r.path("/v1/notification/**")
                //         .filters(f -> f.filter(filter))
                //         .uri("lb://notification-service"))

                // .route("auth-service", r -> r.path("/v1/auth/**")
                //         .uri("lb://auth-service"))

                // .route("file-storage", r -> r.path("/v1/file-storage/**")
                //         .filters(f -> f.filter(filter))
                //         .uri("lb://file-storage"))
                .build();
    }
}
