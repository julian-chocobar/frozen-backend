package com.enigcode.frozen_backend.recipes.controller;

import com.enigcode.frozen_backend.recipes.DTO.RecipeCreateDTO;
import com.enigcode.frozen_backend.recipes.DTO.RecipeResponseDTO;
import com.enigcode.frozen_backend.recipes.DTO.RecipeUpdateDTO;
import com.enigcode.frozen_backend.recipes.service.RecipeService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recipes")
@RequiredArgsConstructor
public class RecipeController {

    final RecipeService recipeService;

    @Operation(
        summary = "Registrar receta",
        description = "Registra una nueva receta en la base de datos")
    @PostMapping
    public ResponseEntity<RecipeResponseDTO> createRecipe(
            @Valid @RequestBody RecipeCreateDTO recipeCreateDTO){
        
        RecipeResponseDTO recipeResponseDTO = recipeService.createRecipe(recipeCreateDTO);

        return new ResponseEntity<>(recipeResponseDTO, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Modificar receta",
        description = "Modifica una receta en especifico segun el id ")
    @PatchMapping("/{id}")
    public ResponseEntity<RecipeResponseDTO> updateRecipe(@PathVariable Long id,
         @Valid @RequestBody RecipeUpdateDTO recipeUpdateDTO) {
        RecipeResponseDTO recipeResponseDTO = recipeService.updateRecipe(id, recipeUpdateDTO);
        return new ResponseEntity<>(recipeResponseDTO, HttpStatus.OK);
    }

    @Operation(
        summary = "Eliminar receta",
        description = "Elimina una receta segun su id")
    @DeleteMapping("/{id}")
    public ResponseEntity<RecipeResponseDTO> deleteRecipe(@PathVariable Long id){
        RecipeResponseDTO recipeResponseDTO = recipeService.deleteRecipe(id);

        return new ResponseEntity<>(recipeResponseDTO, HttpStatus.NO_CONTENT);
    }

    @Operation(
        summary = "Obtener receta",
        description = "Obtiene una receta a partir de su id"
    )
    @GetMapping("/{id}")
    public ResponseEntity<RecipeResponseDTO> getRecipe(@PathVariable Long id){
        RecipeResponseDTO recipeResponseDTO = recipeService.getRecipe(id);

        return new ResponseEntity<>(recipeResponseDTO,HttpStatus.OK);
    }
    
    @Operation(
        summary = "Obtener lista simple de recetas",
        description = "Obtiene una lista simple con id y nombre de todas las recetas activas"
    )
    @GetMapping("/list")
    public ResponseEntity<List<RecipeResponseDTO>> getRecipeList() {
        List<RecipeResponseDTO> recipeResponseDTOs = recipeService.getRecipeList();
        return ResponseEntity.ok(recipeResponseDTOs);
    }

    @Operation(
        summary = "Obtener materiales por fase",
        description = "Obtiene una lista de materiales asociados a una fase segun el id de la fase"
    )
    @GetMapping("/by-product-phase/{id}")
    public ResponseEntity<List<RecipeResponseDTO>> getMaterialByPhase(@PathVariable Long id) {
        List<RecipeResponseDTO> materialsByPhase = recipeService.getMaterialByPhase(id);
        return ResponseEntity.ok(materialsByPhase);
    }


    @Operation(
        summary = "Obtener materiales por producto",
        description = "Obtiene una lista de materiales asociados a un producto segun el id de la producto"
    )
    @GetMapping("/by-product/{id}")
    public ResponseEntity<List<RecipeResponseDTO>> getMaterialByProduct(@PathVariable Long id) {
        List<RecipeResponseDTO> materialsByProduct = recipeService.getMaterialByProduct(id);
        return ResponseEntity.ok(materialsByProduct);
    }

}
