package com.chess.tms.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter, CustomUserDetailsService customUserDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customUserDetailsService = customUserDetailsService;
    }

    // Password encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Authentication provider
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // Authentication manager
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, DaoAuthenticationProvider provider) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(provider)
                .build();
    }

    // Security filter chain
        @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())  // Disable CSRF for stateless API
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                // ===== Authentication & Registration Endpoints =====
                .requestMatchers("/api/auth/**", "/api/users/register/**", "/api/otp/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                
                // ===== Admin-Protected Endpoints =====
                .requestMatchers("/api/auth/register/admin").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/tournaments/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/tournaments/**").hasAuthority("ADMIN")
                .requestMatchers("/admin/**").hasAuthority("ADMIN")
                
                // ===== Tournament & Game Related Public Endpoints =====
                .requestMatchers("/api/matches/**").permitAll()
                .requestMatchers("/api/player/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/tournaments/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/tournaments/**").permitAll()
                .requestMatchers("/api/tournament-players/**").permitAll()
                .requestMatchers("/api/game-type/**").permitAll()
                .requestMatchers("/api/round-type/**").permitAll()
                .requestMatchers("/api/tournament-type/**").permitAll()
                .requestMatchers("/api/swiss-bracket/**").permitAll()
                .requestMatchers("/api/swiss-standing/**").permitAll()
                
                // ===== User & Utility Endpoints =====
                .requestMatchers("/api/user/**").permitAll()
                .requestMatchers("/api/elo/**").permitAll()
                .requestMatchers("/api/s3/**").permitAll()

                // ===== Health Check Endpoints =====
                .requestMatchers("/health").permitAll()
                
                // Require authentication for all other endpoints
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // Use stateless sessions for JWT
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("*");  // Allow all origins
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));  // Allow standard HTTP methods
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));  // Allow necessary headers
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);  // Apply to all paths
        return source;
    }
}