package com.chess.tms.user_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.chess.tms.user_service.model.UserDetails;

public interface UserDetailsRepository extends JpaRepository<UserDetails, Long> {
    Optional<UserDetails> findByUserCredentialsId(Long userId);
}