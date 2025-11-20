package com.enigcode.frozen_backend.analytics.controller;

import com.enigcode.frozen_backend.analytics.DTO.MonthlyTotalDTO;
import com.enigcode.frozen_backend.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "Ver produccion por mes",
                description = "Devuelve una lista de la suma de produccion por mes del ultimo año (por default) y se" +
                        "puede filtrar por fechas y producto")
    @GetMapping("/monthly-production")
    public ResponseEntity<List<MonthlyTotalDTO>> getMonthlyProduction(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Long productId
    ) {
        List<MonthlyTotalDTO> dtoList = analyticsService.getMonthlyProduction(startDate, endDate, productId);
        return new ResponseEntity<>(dtoList, HttpStatus.OK);
    }

    @Operation(summary = "Ver materiales consumidos por mes",
            description = "Devuelve una lista de la suma de materiales consumidos por mes del ultimo año (por default) " +
                    "y se puede filtrar por fechas y producto")
    @GetMapping("/monthly-material-consumption")
    public ResponseEntity<List<MonthlyTotalDTO>> getMonthlyMaterialConsumption(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Long materialId
    ) {
        List<MonthlyTotalDTO> dtoList = analyticsService.getMonthlyMaterialConsumption(startDate, endDate, materialId);
        return new ResponseEntity<>(dtoList, HttpStatus.OK);
    }
}
