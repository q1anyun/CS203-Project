package com.chess.tms.gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
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

    @Value("${client.url}")
    private String clientUrl;

    public SecurityConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter, CustomUserDetailsService customUserDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, DaoAuthenticationProvider provider) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(provider)
                .build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())  // Disable CSRF
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Apply CORS configuration
            .authorizeHttpRequests(auth -> auth
                // .anyRequest().permitAll() 

                // only permit the creation of admin by anotnher admin
                .requestMatchers("/api/auth/register/admin").hasAuthority("ADMIN")
                .requestMatchers("/api/auth/**", "/api/users/register/**").permitAll()  // Permit login and registration to everyone
                .requestMatchers("/api/auth/**").permitAll()  // Permit login and registration to everyone
                .requestMatchers("/api/matches/**").permitAll()
                .requestMatchers("/api/player/**").permitAll()
                .requestMatchers(HttpMethod.GET,"/api/tournaments/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.POST,"/api/tournaments/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.PUT,"/api/tournaments/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.DELETE,"/api/tournaments/**").hasAuthority("ADMIN")
                .requestMatchers("/api/tournament-players/**").permitAll()
                .requestMatchers("/api/game-type/**").permitAll() 
                .requestMatchers("/api/round-type/**").permitAll()
                // Only admins can access /admin/** endpoints
                .requestMatchers("/admin/**").hasAuthority("ADMIN")
                // Users and admins can access /user/** endpoints
                .requestMatchers("api/users/**").hasAuthority("Player")
               .anyRequest().authenticated()  // All other requests require authentication
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // Set session to be stateless (JWT)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin(clientUrl);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}