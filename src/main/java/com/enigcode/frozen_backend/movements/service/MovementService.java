package com.enigcode.frozen_backend.movements.service;

import com.enigcode.frozen_backend.movements.DTO.*;
import com.enigcode.frozen_backend.movements.model.MovementType;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.validation.Valid;

import java.util.List;

public interface MovementService {
    MovementResponseDTO createMovement(@Valid MovementCreateDTO movementCreateDTO);
    void createReserveOrReturn (MovementType type, List<MovementSimpleCreateDTO> materials);
    @Transactional
    void confirmReservation(@Valid List<MovementSimpleCreateDTO> materials);
    MovementDetailDTO getMovement(Long id);
    Page<MovementResponseDTO> findAll(MovementFilterDTO filterDTO, Pageable pageable);
}
