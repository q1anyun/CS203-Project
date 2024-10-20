package com.chess.tms.tournament_service.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration {
    @Value("${client.url}")
    private String clientUrl;
   
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                    http
                    .csrf(csrf -> csrf.disable())  // Disable CSRF
                    .authorizeHttpRequests(auth -> auth
                         .anyRequest().permitAll() 
                    );
                
            return http.build();
        }
}
