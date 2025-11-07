package com.enigcode.frozen_backend.production_materials.controller;

import com.enigcode.frozen_backend.production_materials.DTO.ProductionMaterialResponseDTO;
import com.enigcode.frozen_backend.production_materials.service.ProductionMaterialService;
import com.enigcode.frozen_backend.recipes.DTO.RecipeResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/production-materials")
@RequiredArgsConstructor
public class ProductionMaterialController {
    final private ProductionMaterialService productionMaterialService;

    @Operation(summary = "Obtener material en produccion",
            description = "Obtiene un material en produccion a partir de su id")
    @GetMapping("/{id}")
    public ResponseEntity<ProductionMaterialResponseDTO> getProductionMaterial(@PathVariable Long id) {
            ProductionMaterialResponseDTO dto = productionMaterialService.getProductionMaterial(id);

            return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @Operation(summary = "Obtener materiales por fase",
            description = "Obtiene una lista de materiales en produccion asociados a una fase segun el id de la fase")
    @GetMapping("/by-production-phase/{id}")
    public ResponseEntity<List<ProductionMaterialResponseDTO>> getProductionMaterialByPhase(@PathVariable Long id) {
        List<ProductionMaterialResponseDTO> dto = productionMaterialService.getProductionMaterialByPhase(id);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @Operation(summary = "Obtener materiales produccion por lote",
            description = "Obtiene una lista de materiales asociados a un lote segun el id del mismo")
    @GetMapping("/by-batch/{id}")
    public ResponseEntity<List<ProductionMaterialResponseDTO>> getProductionMaterialByBatch(@PathVariable Long id) {
        List<ProductionMaterialResponseDTO> dto = productionMaterialService.getProductionMaterialByBatch(id);

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }
}
