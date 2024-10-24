package com.chess.tms.s3_upload_service.security; 

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration {
   
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
