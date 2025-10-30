package com.enigcode.frozen_backend.movements.repository;

import com.enigcode.frozen_backend.movements.model.Movement;
import com.enigcode.frozen_backend.movements.model.MovementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MovementRepository extends JpaRepository<Movement, Long>, JpaSpecificationExecutor<Movement> {

    /**
     * Busca movimientos por estado, ordenados por fecha de creación
     */
    Page<Movement> findByStatusOrderByCreationDateAsc(MovementStatus status, Pageable pageable);
}