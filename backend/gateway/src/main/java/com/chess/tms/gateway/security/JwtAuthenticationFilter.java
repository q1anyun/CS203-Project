package com.chess.tms.gateway.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtility jwtUtility;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtility jwtUtility, UserDetailsService userDetailsService,
            HandlerExceptionResolver handlerExceptionResolver) {
        this.jwtUtility = jwtUtility;
        this.userDetailsService = userDetailsService;
    }

    // Filter to handle JWT authentication
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        System.out.println("================================================");
        System.out.println("Request URL: " + request.getRequestURL());
        System.out.println("================================================");
        
        // Check if the Authorization header is present and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("No Token found, process anyways");
            filterChain.doFilter(request, response);
            return;
        }

        // Extract and validate the JWT token
        try {
            String jwt = authHeader.substring(7);
            final String userEmail = jwtUtility.extractUsername(jwt);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Check if the user email is present and there's no existing authentication
            if (userEmail != null && authentication == null && userEmail != "") {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if (jwtUtility.validateToken(jwt, userDetails)) {
                    // Create an authentication token for the user
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

                    // Set the authentication details for the request
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    // Continue with the next filter in the chain
                    filterChain.doFilter(request, response);
                } else {
                    // Invalid JWT token
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid JWT token");
                    return;
                }
            }
        } catch (Exception exception) {
            // Handle exceptions by sending an unauthorized error
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");   
        }
    }
}