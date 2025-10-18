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
import com.enigcode.frozen_backend.movements.repository.MovementRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

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
public class MovementServiceImpl implements MovementService {

        final MovementRepository movementRepository;
        final MaterialRepository materialRepository;
        final MovementMapper movementMapper;

        /**
         * Funcion que genera un movimiento nuevo que afecta al stock disponible de un material (ingreso-egreso)
         * y cambia el stock del material según el mismo
         * Genera Error en caso de que el stock no sea suficiente para abastecer el
         * movimiento o si el material
         * no existe
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

                if (movementCreateDTO.getType().equals(MovementType.EGRESO) &&
                                movementCreateDTO.getStock() > material.getStock())
                        throw new BadRequestException("El stock actual (" + material.getStock() +
                                        ") es insuficiente para egresar " + movementCreateDTO.getStock());

                if (movementCreateDTO.getType().equals(MovementType.EGRESO))
                        material.reduceStock(movementCreateDTO.getStock());
                else if (movementCreateDTO.getType().equals(MovementType.INGRESO))
                        material.increaseStock(movementCreateDTO.getStock());
                else throw new BadRequestException("El tipo de movimiento " + movementCreateDTO.getType()
                                + "no es soportado en esta función");

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

        /**
         * Funcion que crea nuevos movimientos de reserva o devuelto de stock debe
         * llamarse en el contexto de un transaccional
         * @param type Tiene que ser RESERVA o DEVUELTO
         * @param materials es una List<MovementSimpleCreateDTO> donde tiene material y stock
         */
        @Override
        public void createReserveOrReturn (@NotNull MovementType type,@Valid List<MovementSimpleCreateDTO> materials){
                List<Movement> movements = new ArrayList<>();

                materials.forEach(dto -> {
                        if (type.equals(MovementType.RESERVA) && dto.getMaterial().getStock() < dto.getStock())
                                throw new BadRequestException("Stock: "+dto.getMaterial().getStock()
                                        + "insuficiente para reservar " + dto.getStock());

                        if (type.equals(MovementType.DEVUELTO) && dto.getMaterial().getReservedStock() < dto.getStock())
                                throw new BadRequestException("Stock reservado: "+dto.getMaterial().getStock()
                                        + "insuficiente para devolver " + dto.getStock());

                        if (type.equals(MovementType.RESERVA)) dto.getMaterial().reserveStock(dto.getStock());
                        else if (type.equals(MovementType.DEVUELTO)) dto.getMaterial().returnStock(dto.getStock());
                        else throw new BadRequestException("Esta función no acepta movimientos del tipo " + type);

                        Movement movement = Movement.builder()
                                .type(type)
                                .stock(dto.getStock())
                                .reason("El stock se fue :" + type)
                                .realizationDate(OffsetDateTime.now(ZoneOffset.UTC))
                                .material(dto.getMaterial()).build();

                        movements.add(movement);
                });

                movementRepository.saveAllAndFlush(movements);
        }

        /**
         * Funcion utilizada para crear los movimientos de egreso a partir del stock reservado de un producto
         * se debe en contexto de transaccional
         * @param materials es una List<MovementSimpleCreateDTO> donde tiene material y stock
         */
        @Override
        public void confirmReservation(@Valid List<MovementSimpleCreateDTO> materials){
                List<Movement> movements = new ArrayList<>();

                materials.forEach(dto -> {
                        if(dto.getMaterial().getReservedStock() < dto.getStock())
                                throw new BadRequestException("Stock reservado: "+dto.getMaterial().getStock()
                                        + "insuficiente para egresar " + dto.getStock());

                        dto.getMaterial().reduceReservedStock(dto.getStock());

                        Movement movement = Movement.builder()
                                .type(MovementType.EGRESO)
                                .stock(dto.getStock())
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
}
