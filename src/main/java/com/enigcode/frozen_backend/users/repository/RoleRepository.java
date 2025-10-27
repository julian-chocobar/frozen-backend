package com.enigcode.frozen_backend.users.repository;

import com.enigcode.frozen_backend.users.model.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByName(String name);

    Set<RoleEntity> findByNameIn(Set<String> names);

    boolean existsByName(String name);
}