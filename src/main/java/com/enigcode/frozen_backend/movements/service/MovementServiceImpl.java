package com.enigcode.frozen_backend.movements.service;

import com.enigcode.frozen_backend.movements.mapper.MovementMapper;
import com.enigcode.frozen_backend.movements.repository.MovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MovementServiceImpl implements MovementService{

    final MovementRepository movementRepository;
    final MovementMapper movementMapper;
}
