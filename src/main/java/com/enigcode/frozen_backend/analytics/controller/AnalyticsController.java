package com.enigcode.frozen_backend.analytics.controller;

import com.enigcode.frozen_backend.analytics.DTO.DashboardStatsDTO;
import com.enigcode.frozen_backend.analytics.DTO.MonthlyTotalDTO;
import com.enigcode.frozen_backend.analytics.service.AnalyticsService;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/analytics")
public class AnalyticsController {

        private final AnalyticsService analyticsService;

        @Operation(summary = "Ver produccion por mes", description = "Devuelve una lista de la suma de produccion por mes del ultimo año (por default) y se"
                        +
                        "puede filtrar por fechas y producto")
        @GetMapping("/monthly-production")
        @PreAuthorize("hasRole('GERENTE_GENERAL')")
        public ResponseEntity<List<MonthlyTotalDTO>> getMonthlyProduction(
                        @RequestParam(required = false) LocalDate startDate,
                        @RequestParam(required = false) LocalDate endDate,
                        @RequestParam(required = false) Long productId) {
                List<MonthlyTotalDTO> dtoList = analyticsService.getMonthlyProduction(startDate, endDate, productId);
                return new ResponseEntity<>(dtoList, HttpStatus.OK);
        }

        @Operation(summary = "Ver materiales consumidos por mes", description = "Devuelve una lista de la suma de materiales consumidos por mes del ultimo año (por default) "
                        +
                        "y se puede filtrar por fechas y producto")
        @GetMapping("/monthly-material-consumption")
        @PreAuthorize("hasRole('GERENTE_GENERAL')")
        public ResponseEntity<List<MonthlyTotalDTO>> getMonthlyMaterialConsumption(
                        @RequestParam(required = false) LocalDate startDate,
                        @RequestParam(required = false) LocalDate endDate,
                        @RequestParam(required = false) Long materialId) {
                List<MonthlyTotalDTO> dtoList = analyticsService.getMonthlyMaterialConsumption(startDate, endDate,
                                materialId);
                return new ResponseEntity<>(dtoList, HttpStatus.OK);
        }

        @Operation(summary = "Ver desperdicios por mes", description = "Devuelve una lista de la suma de los desperdicios generados por mes del ultimo año (default)"
                        +
                        " y se puede filtrar por fechas y fase")
        @GetMapping("/monthly-waste")
        @PreAuthorize("hasRole('GERENTE_GENERAL')")
        public ResponseEntity<List<MonthlyTotalDTO>> getMonthlyWaste(
                        @RequestParam(required = false) LocalDate startDate,
                        @RequestParam(required = false) LocalDate endDate,
                        @RequestParam(required = false) Phase phase,
                        @RequestParam(defaultValue = "false") boolean transferOnly) {
                List<MonthlyTotalDTO> dtoList = analyticsService.getMonthlyWaste(startDate, endDate, phase,
                                transferOnly);
                return new ResponseEntity<>(dtoList, HttpStatus.OK);
        }

        @Operation(summary = "Ver resumen del ultimo mes", description = "Devuelve diferentes datos relacionados al ultimo mes")
        @GetMapping("/dashboard/monthly")
        @PreAuthorize("hasRole('GERENTE_GENERAL')")
        public ResponseEntity<DashboardStatsDTO> getMonthlyDashboard() {
                DashboardStatsDTO dto = analyticsService.getDashboardStats();
                return new ResponseEntity<>(dto, HttpStatus.OK);
        }

        @Operation(summary = "Ver eficiencia neta por mes", description = "Devuelve la eficiencia neta de producción (%) por mes del último año (por default). "
                        + "Se puede filtrar por fechas, producto y fase. "
                        + "Sin filtro de fase: Eficiencia = (Producción Final ENVASADO / Materiales Totales) × 100. "
                        + "Con filtro de fase: Eficiencia = (Output de la fase / Input de la fase) × 100. "
                        + "Representa el porcentaje de material que se convierte en producto útil.")
        @GetMapping("/monthly-efficiency")
        public ResponseEntity<List<MonthlyTotalDTO>> getMonthlyEfficiency(
                        @RequestParam(required = false) LocalDate startDate,
                        @RequestParam(required = false) LocalDate endDate,
                        @RequestParam(required = false) Long productId,
                        @RequestParam(required = false) Phase phase) {
                List<MonthlyTotalDTO> dtoList = analyticsService.getMonthlyEfficiency(startDate, endDate, productId, phase);
                return new ResponseEntity<>(dtoList, HttpStatus.OK);
        }

}
