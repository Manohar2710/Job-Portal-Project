package com.learning.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learning.security.entity.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
