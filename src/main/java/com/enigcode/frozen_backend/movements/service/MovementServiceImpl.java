package com.enigcode.frozen_backend.movements.service;

import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.BadRequestException;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.repository.MaterialRepository;
import com.enigcode.frozen_backend.movements.DTO.*;
import com.enigcode.frozen_backend.movements.specification.MovementSpecification;
import com.enigcode.frozen_backend.movements.mapper.MovementMapper;
import com.enigcode.frozen_backend.movements.model.Movement;
import com.enigcode.frozen_backend.movements.model.MovementType;
import com.enigcode.frozen_backend.movements.model.MovementStatus;
import com.enigcode.frozen_backend.movements.repository.MovementRepository;
import com.enigcode.frozen_backend.notifications.service.NotificationService;
import com.enigcode.frozen_backend.users.service.UserService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovementServiceImpl implements MovementService {

        final MovementRepository movementRepository;
        final MaterialRepository materialRepository;
        final MovementMapper movementMapper;
        final NotificationService notificationService;
        final UserService userService;

        /**
         * Funcion que genera un movimiento nuevo en estado PENDIENTE
         * Los movimientos ahora se crean como pendientes y deben ser completados por un
         * operario
         * Genera notificación para operarios de almacén
         * 
         * @param movementCreateDTO
         * @return movementResponseDTO
         */
        @Override
        @Transactional
        public MovementResponseDTO createMovement(@Valid MovementCreateDTO movementCreateDTO) {
                Material material = materialRepository.findById(movementCreateDTO.getMaterialId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Material no encontrado con ID: " + movementCreateDTO.getMaterialId()));

                // Validar que hay stock suficiente para egresos (aunque aún no se ejecute)
                if (movementCreateDTO.getType().equals(MovementType.EGRESO) &&
                                movementCreateDTO.getStock() > material.getStock())
                        throw new BadRequestException("El stock actual (" + material.getStock() +
                                        ") es insuficiente para egresar " + movementCreateDTO.getStock());

                // Crear movimiento en estado PENDIENTE
                Movement movement = Movement.builder()
                                .type(movementCreateDTO.getType())
                                .stock(movementCreateDTO.getStock())
                                .reason(movementCreateDTO.getReason())
                                .location(movementCreateDTO.getLocation())
                                .createdByUserId(userService.getCurrentUser().getId())
                                .status(MovementStatus.PENDIENTE)
                                .material(material)
                                .creationDate(OffsetDateTime.now(ZoneOffset.UTC))
                                .build();

                Movement savedMovement = movementRepository.saveAndFlush(movement);

                // Crear notificación para operarios de almacén
                notificationService.createPendingMovementNotification(
                                savedMovement.getId(),
                                material.getName(),
                                movementCreateDTO.getType().toString());

                log.info("Movimiento {} creado en estado PENDIENTE para material: {}",
                                savedMovement.getId(), material.getName());

                return movementMapper.toResponseDto(savedMovement);
        }

        /**
         * Funcion que crea nuevos movimientos de reserva o devuelto de stock debe
         * llamarse en el contexto de un transaccional
         * 
         * @param type      Tiene que ser RESERVA o DEVUELTO
         * @param materials es una List<MovementSimpleCreateDTO> donde tiene material y
         *                  stock
         */
        @Override
        public void createReserveOrReturn(@NotNull MovementType type, @Valid List<MovementSimpleCreateDTO> materials) {
                List<Movement> movements = new ArrayList<>();

                materials.forEach(dto -> {
                        if (type.equals(MovementType.RESERVA) && dto.getMaterial().getStock() < dto.getStock())
                                throw new BadRequestException("Stock: " + dto.getMaterial().getStock()
                                                + "insuficiente para reservar " + dto.getStock());

                        if (type.equals(MovementType.DEVUELTO) && dto.getMaterial().getReservedStock() < dto.getStock())
                                throw new BadRequestException("Stock reservado: " + dto.getMaterial().getStock()
                                                + "insuficiente para devolver " + dto.getStock());

                        if (type.equals(MovementType.RESERVA))
                                dto.getMaterial().reserveStock(dto.getStock());
                        else if (type.equals(MovementType.DEVUELTO))
                                dto.getMaterial().returnStock(dto.getStock());
                        else
                                throw new BadRequestException("Esta función no acepta movimientos del tipo " + type);

                        Movement movement = Movement.builder()
                                        .type(type)
                                        .stock(dto.getStock())
                                        .status(MovementStatus.COMPLETADO)
                                        .reason("El stock se fue :" + type)
                                        .realizationDate(OffsetDateTime.now(ZoneOffset.UTC))
                                        .material(dto.getMaterial()).build();

                        movements.add(movement);
                });

                movementRepository.saveAllAndFlush(movements);
        }

        /**
         * Funcion utilizada para crear los movimientos de egreso a partir del stock
         * reservado de un producto
         * se debe en contexto de transaccional
         * 
         * @param materials es una List<MovementSimpleCreateDTO> donde tiene material y
         *                  stock
         */
        @Override
        public void confirmReservation(@Valid List<MovementSimpleCreateDTO> materials) {
                List<Movement> movements = new ArrayList<>();

                materials.forEach(dto -> {
                        if (dto.getMaterial().getReservedStock() < dto.getStock())
                                throw new BadRequestException("Stock reservado: " + dto.getMaterial().getStock()
                                                + "insuficiente para egresar " + dto.getStock());

                        dto.getMaterial().reduceReservedStock(dto.getStock());

                        Movement movement = Movement.builder()
                                        .type(MovementType.EGRESO)
                                        .stock(dto.getStock())
                                        .status(MovementStatus.COMPLETADO)
                                        .reason("El stock salio de reserva ")
                                        .realizationDate(OffsetDateTime.now(ZoneOffset.UTC))
                                        .material(dto.getMaterial()).build();

                        movements.add(movement);
                });

                movementRepository.saveAllAndFlush(movements);
        }

        /**
         * Funcion que devuelve un movimiento con detalles especificos
         * 
         * @param id
         * @return MovementDetailDTO
         */
        @Override
        @Transactional
        public MovementDetailDTO getMovement(Long id) {
                Movement movement = movementRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Movimiento no encontrado con ID: " + id));

                return movementMapper.toDetailDTO(movement);
        }

        @Override
        public Page<MovementResponseDTO> findAll(MovementFilterDTO filterDTO, Pageable pageable) {
                Pageable pageRequest = PageRequest.of(
                                pageable.getPageNumber(),
                                pageable.getPageSize(),
                                pageable.getSort());
                Page<Movement> movements = movementRepository.findAll(
                                MovementSpecification.createFilter(filterDTO), pageRequest);
                return movements.map(movementMapper::toResponseDto);
        }

        /**
         * Completa un movimiento pendiente ejecutando el cambio de stock
         * y marcando el movimiento como completado
         */
        @Override
        @Transactional
        public MovementResponseDTO completeMovement(Long movementId) {
                Movement movement = movementRepository.findById(movementId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Movimiento no encontrado con ID: " + movementId));

                if (movement.getStatus() != MovementStatus.PENDIENTE &&
                                movement.getStatus() != MovementStatus.EN_PROCESO) {
                        throw new BadRequestException("El movimiento ya está completado o no es válido");
                }

                // Validar que si el movimiento está EN_PROCESO, solo el usuario que lo marcó
                // puede completarlo
                if (movement.getStatus() == MovementStatus.EN_PROCESO) {
                        Long currentUserId = userService.getCurrentUser().getId();
                        if (movement.getInProgressByUserId() != null
                                        && !movement.getInProgressByUserId().equals(currentUserId)) {
                                throw new BadRequestException(
                                                "Solo el usuario que marcó este movimiento como 'En Proceso' puede completarlo");
                        }
                }

                Material material = movement.getMaterial();

                // Validar stock nuevamente en caso de que haya cambiado
                if (movement.getType().equals(MovementType.EGRESO) &&
                                movement.getStock() > material.getStock()) {
                        throw new BadRequestException("Stock insuficiente para completar el egreso. " +
                                        "Stock actual: " + material.getStock() +
                                        ", Stock requerido: " + movement.getStock());
                }

                // Ejecutar el cambio de stock
                if (movement.getType().equals(MovementType.EGRESO)) {
                        material.reduceStock(movement.getStock());

                        // Verificar si el material queda por debajo del umbral
                        if (material.getStock() < material.getThreshold()) {
                                notificationService.createLowStockNotification(
                                                material.getId(),
                                                material.getName(),
                                                material.getStock(),
                                                material.getThreshold());
                                log.warn("Material {} quedó por debajo del umbral. Stock actual: {}, Umbral: {}",
                                                material.getName(), material.getStock(), material.getThreshold());
                        }
                } else if (movement.getType().equals(MovementType.INGRESO)) {
                        material.increaseStock(movement.getStock());
                }

                // Marcar movimiento como completado
                movement.completeMovement(userService.getCurrentUser().getId());
                movement.setRealizationDate(OffsetDateTime.now(ZoneOffset.UTC));

                // Guardar cambios
                materialRepository.save(material);
                Movement savedMovement = movementRepository.save(movement);

                log.info("Movimiento {} completado por usuario: {}. Material: {}, Tipo: {}, Cantidad: {}",
                                savedMovement.getId(), userService.getCurrentUser().getUsername(),
                                material.getName(), movement.getType(), movement.getStock());

                return movementMapper.toResponseDto(savedMovement);
        }

        @Override
        @Transactional
        public MovementResponseDTO toggleInProgressPending(Long movementId) {
                Movement movement = movementRepository.findById(movementId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Movimiento no encontrado con ID: " + movementId));

                if (movement.getStatus() == MovementStatus.COMPLETADO) {
                        throw new BadRequestException("Los movimientos completados no pueden cambiar de estado");

                } else if (movement.getStatus() == MovementStatus.EN_PROCESO) {
                        Long currentUserId = userService.getCurrentUser().getId();

                        // Validar que solo el usuario que puso en proceso pueda revertir a pendiente
                        if (movement.getInProgressByUserId() != null
                                        && !movement.getInProgressByUserId().equals(currentUserId)) {
                                throw new BadRequestException(
                                                "Solo el usuario que marcó este movimiento como 'En Proceso' puede revertirlo a 'Pendiente'");
                        }

                        movement.setStatus(MovementStatus.PENDIENTE);
                        movement.setInProgressByUserId(null);
                        movement.setTakenAt(null);

                        Movement savedMovement = movementRepository.save(movement);

                        log.info("Movimiento {} revertido a PENDIENTE por usuario: {}",
                                        savedMovement.getId(), userService.getCurrentUser().getUsername());

                        return movementMapper.toResponseDto(savedMovement);

                } else if (movement.getStatus() == MovementStatus.PENDIENTE) {
                        Long currentUserId = userService.getCurrentUser().getId();

                        // Validar que si ya hay un usuario en proceso, solo ese usuario pueda cambiar
                        // el estado
                        if (movement.getInProgressByUserId() != null
                                        && !movement.getInProgressByUserId().equals(currentUserId)) {
                                throw new BadRequestException(
                                                "Este movimiento ya está siendo procesado por otro usuario");
                        }

                        movement.setStatus(MovementStatus.EN_PROCESO);
                        movement.setInProgressByUserId(currentUserId);
                        movement.setTakenAt(OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime());

                        Movement savedMovement = movementRepository.save(movement);

                        log.info("Movimiento {} marcado como EN_PROCESO por usuario: {}",
                                        savedMovement.getId(), userService.getCurrentUser().getUsername());

                        return movementMapper.toResponseDto(savedMovement);
                } else {
                        throw new BadRequestException("Estado de movimiento no válido");
                }

        }

}
