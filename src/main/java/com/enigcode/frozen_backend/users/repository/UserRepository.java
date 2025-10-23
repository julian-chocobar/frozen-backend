package com.enigcode.frozen_backend.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.enigcode.frozen_backend.users.model.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}
