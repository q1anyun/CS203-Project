package com.chess.tms.user_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chess.tms.user_service.model.Users;

public interface UsersRepository extends JpaRepository<Users, Long> {
        Optional<Users> findByUsername(String username);
        Optional<Users> findByEmail(String email);
}
