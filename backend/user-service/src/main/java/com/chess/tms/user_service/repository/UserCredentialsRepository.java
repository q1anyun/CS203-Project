package com.chess.tms.user_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.chess.tms.user_service.model.UserCredentials;

public interface UserCredentialsRepository extends JpaRepository<UserCredentials, Long> {
        Optional<UserCredentials> findByUsername(String username);
        Optional<UserCredentials> findByEmail(String email);
}
