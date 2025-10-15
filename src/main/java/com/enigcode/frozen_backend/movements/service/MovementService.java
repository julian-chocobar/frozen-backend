package com.enigcode.frozen_backend.movements.service;

import com.enigcode.frozen_backend.movements.DTO.MovementCreateDTO;
import com.enigcode.frozen_backend.movements.DTO.MovementDetailDTO;
import com.enigcode.frozen_backend.movements.DTO.MovementResponseDTO;
import com.enigcode.frozen_backend.movements.DTO.MovementFilterDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.validation.Valid;

public interface MovementService {
    MovementResponseDTO createMovement(@Valid MovementCreateDTO movementCreateDTO);

    MovementDetailDTO getMovement(Long id);

    Page<MovementResponseDTO> findAll(MovementFilterDTO filterDTO, Pageable pageable);
}
