package com.chess.tms.api_gateway.security;

import reactor.core.publisher.Mono;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import java.util.List;
import java.util.function.Predicate;

@Component
public class JwtAuthenticationFilter implements GatewayFilter {

    private final JwtUtility jwtUtility;

    public JwtAuthenticationFilter(JwtUtility jwtUtility) {
        this.jwtUtility = jwtUtility;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("Request: ");
        ServerHttpRequest request = exchange.getRequest();

            String token = request.getHeaders().getOrEmpty("Authorization").get(0);

            if (token != null && token.startsWith("Bearer ")) token = token.substring(7);

            try {
                jwtUtility.validateToken(token);
            } catch (Exception e) {
                return onError(exchange);
            }
        
        return chain.filter(exchange);
    }

    private Mono<Void> onError(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.NOT_FOUND);
        return response.setComplete();
    }

    private boolean authMissing(ServerHttpRequest request) {
        return !request.getHeaders().containsKey("Authorization");
    }

    // @Override
    // protected void doFilterInternal(
    //         @NonNull HttpServletRequest request,
    //         @NonNull HttpServletResponse response,
    //         @NonNull FilterChain filterChain
    // ) throws ServletException, IOException {
    //     final String authHeader = request.getHeader("Authorization");

    //     if (authHeader == null || !authHeader.startsWith("Bearer ")) {
    //         filterChain.doFilter(request, response);
    //         return;
    //     }

    //     try {
    //         final String jwt = authHeader.substring(7);
    //         final String userEmail = jwtUtility.extractUsername(jwt);

    //         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    //         if (userEmail != null && authentication == null) {
    //             UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

    //             if (jwtUtility.validateToken(jwt, userDetails)) {
    //                 UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
    //                         userDetails,
    //                         null,
    //                         userDetails.getAuthorities()
    //                 );

    //                 authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    //                 SecurityContextHolder.getContext().setAuthentication(authToken);
    //             }else {
    //                 // Invalid JWT token - handle as you see fit, or return 404 if appropriate
    //                 response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid JWT token");
    //                 return;
    //             }
    //         }

    //         filterChain.doFilter(request, response);
    //     } catch (Exception exception) {
    //         handlerExceptionResolver.resolveException(request, response, null, exception);
    //     }
    }