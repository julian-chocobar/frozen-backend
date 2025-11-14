package com.enigcode.frozen_backend.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.enigcode.frozen_backend.users.model.User;
import com.enigcode.frozen_backend.users.model.Role;
import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    /**
     * Busca IDs de usuarios que tienen un rol específico
     */
    @Query("SELECT u.id FROM User u JOIN u.roles r WHERE r = :role")
    List<Long> findUserIdsByRole(@Param("role") Role role);

    /**
     * Sobrecarga para usar el enum Role directamente
     */
    default List<Long> findUserIdsByRoleName(String roleName) {
        return findUserIdsByRole(Role.valueOf(roleName));
    }

    /**
     * Busca IDs de supervisores que están asignados a un sector específico
     */
    @Query("SELECT s.supervisor.id FROM Sector s WHERE s.id = :sectorId")
    List<Long> findSupervisorIdsBySectorId(@Param("sectorId") Long sectorId);
}
