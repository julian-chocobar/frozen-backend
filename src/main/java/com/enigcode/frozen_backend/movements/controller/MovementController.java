package com.enigcode.frozen_backend.movements.controller;

import com.enigcode.frozen_backend.movements.service.MovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/movements")
@RequiredArgsConstructor
public class MovementController {

    final MovementService movementService;
}
