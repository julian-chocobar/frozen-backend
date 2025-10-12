package com.enigcode.frozen_backend.movements.service;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.repository.MaterialRepository;
import com.enigcode.frozen_backend.movements.DTO.MovementCreateDTO;
import com.enigcode.frozen_backend.movements.DTO.MovementResponseDTO;
import com.enigcode.frozen_backend.movements.mapper.MovementMapper;
import com.enigcode.frozen_backend.movements.model.Movement;
import com.enigcode.frozen_backend.movements.model.MovementType;
import com.enigcode.frozen_backend.movements.repository.MovementRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class MovementServiceImpl implements MovementService{

    final MovementRepository movementRepository;
    final MaterialRepository materialRepository;
    final MovementMapper movementMapper;

    @Override
    @Transactional
    public MovementResponseDTO createMovement(MovementCreateDTO movementCreateDTO) {
        Material material = materialRepository.findById(movementCreateDTO.getMaterialID())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Material no encontrado con ID: " + movementCreateDTO.getMaterialID()));

        if (movementCreateDTO.getType().equals(MovementType.EGRESO) &&
                movementCreateDTO.getStock() > material.getStock())
            throw new BadRequestException("El stock actual (" + material.getStock() +
                    ") es insuficiente para egresar " + movementCreateDTO.getStock());

        if(movementCreateDTO.getType().equals(MovementType.EGRESO)) material.reduceStock(movementCreateDTO.getStock());
        else material.increaseStock(movementCreateDTO.getStock());

        Movement movement = Movement.builder()
                .type(movementCreateDTO.getType())
                .stock(movementCreateDTO.getStock())
                .reason(movementCreateDTO.getReason())
                .realizationDate(OffsetDateTime.now(ZoneOffset.UTC))
                .material(material).build();

        materialRepository.save(material);
        Movement savedMovement = movementRepository.saveAndFlush(movement);

        return movementMapper.toResponseDto(savedMovement);
    }
}
