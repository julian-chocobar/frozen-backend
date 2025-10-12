package com.enigcode.frozen_backend.movements.service;

import com.enigcode.frozen_backend.movements.DTO.MovementCreateDTO;
import com.enigcode.frozen_backend.movements.DTO.MovementDetailDTO;
import com.enigcode.frozen_backend.movements.DTO.MovementResponseDTO;
import jakarta.validation.Valid;

public interface MovementService {
    MovementResponseDTO createMovement(@Valid MovementCreateDTO movementCreateDTO);

    MovementDetailDTO getMovement(Long id);
}
