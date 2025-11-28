package com.enigcode.frozen_backend.common.service;

import com.enigcode.frozen_backend.product_phases.DTO.ProductPhaseResponseDTO;
import com.enigcode.frozen_backend.products.DTO.ProductResponseDTO;
import com.enigcode.frozen_backend.product_phases.service.ProductPhaseService;
import com.enigcode.frozen_backend.recipes.DTO.RecipeCreateDTO;
import com.enigcode.frozen_backend.recipes.service.RecipeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeDataLoaderService {

    private final RecipeService recipeService;
    private final ProductPhaseService productPhaseService;
    private final MaterialDataLoaderService materialDataLoaderService;

    public void loadRecipes(List<ProductResponseDTO> products) {
        log.info("Cargando recipes para las fases de cada producto...");

        for (ProductResponseDTO product : products) {
            List<ProductPhaseResponseDTO> phases = productPhaseService.getByProduct(product.getId());

            for (ProductPhaseResponseDTO phase : phases) {
                createRecipesForPhase(product.getName(), phase);
            }
        }

        log.info("Recipes cargadas.");
    }

    private void createRecipesForPhase(String productName, ProductPhaseResponseDTO phase) {
        String phaseName = phase.getPhase().name();
        Long phaseId = phase.getId();

        switch (productName) {
            case "Pale Ale Clásica":
                createPaleAleRecipes(phaseName, phaseId);
                break;
            case "Stout Intensa":
                createStoutRecipes(phaseName, phaseId);
                break;
            case "Pale Sin Alcohol":
                createPaleSinAlcoholRecipes(phaseName, phaseId);
                break;
        }
    }

    private void createPaleAleRecipes(String phaseName, Long phaseId) {
        switch (phaseName) {
            case "MOLIENDA":
                RecipeCreateDTO maltaRecipe = RecipeCreateDTO.builder()
                        .productPhaseId(phaseId)
                        .materialId(materialDataLoaderService.getMaltaPaleId())
                        .quantity(190.0)
                        .build();
                recipeService.createRecipe(maltaRecipe);

                RecipeCreateDTO maltaCrystalRecipe = RecipeCreateDTO.builder()
                        .productPhaseId(phaseId)
                        .materialId(materialDataLoaderService.getMaltaCrystalId())
                        .quantity(48.0)
                        .build();
                recipeService.createRecipe(maltaCrystalRecipe);
                break;

            case "MACERACION":
                RecipeCreateDTO aguaRecipe = RecipeCreateDTO.builder()
                        .productPhaseId(phaseId)
                        .materialId(materialDataLoaderService.getAguaId())
                        .quantity(912.0)
                        .build();
                recipeService.createRecipe(aguaRecipe);
                break;

            case "FILTRACION":
                RecipeCreateDTO clarificanteRecipe = RecipeCreateDTO.builder()
                        .productPhaseId(phaseId)
                        .materialId(materialDataLoaderService.getClarificanteId())
                        .quantity(2.0)
                        .build();
                recipeService.createRecipe(clarificanteRecipe);
                break;

            case "COCCION":
                RecipeCreateDTO lupuloRecipe = RecipeCreateDTO.builder()
                        .productPhaseId(phaseId)
                        .materialId(materialDataLoaderService.getLupuloCitraId())
                        .quantity(6.0)
                        .build();
                RecipeCreateDTO aguaRecipe2 = RecipeCreateDTO.builder()
                        .productPhaseId(phaseId)
                        .materialId(materialDataLoaderService.getAguaId())
                        .quantity(50.0)
                        .build();
                recipeService.createRecipe(lupuloRecipe);
                recipeService.createRecipe(aguaRecipe2);
                break;

            case "FERMENTACION":
                RecipeCreateDTO levaduraRecipe = RecipeCreateDTO.builder()
                        .productPhaseId(phaseId)
                        .materialId(materialDataLoaderService.getLevaduraAleId())
                        .quantity(1.5)
                        .build();
                recipeService.createRecipe(levaduraRecipe);
                break;

            case "MADURACION":
            case "GASIFICACION":
                RecipeCreateDTO co2Recipe = RecipeCreateDTO.builder()
                        .productPhaseId(phaseId)
                        .materialId(materialDataLoaderService.getCo2Id())
                        .quantity(12.0)
                        .build();
                recipeService.createRecipe(co2Recipe);
                break;
        }
    }

    private void createStoutRecipes(String phaseName, Long phaseId) {
        switch (phaseName) {
            case "MOLIENDA":
                RecipeCreateDTO maltaRecipe = RecipeCreateDTO.builder()
                        .productPhaseId(phaseId)
                        .materialId(materialDataLoaderService.getMaltaPaleId())
                        .quantity(197.0)
                        .build();
                recipeService.createRecipe(maltaRecipe);

                RecipeCreateDTO maltaCrystalRecipe = RecipeCreateDTO.builder()
                        .productPhaseId(phaseId)
                        .materialId(materialDataLoaderService.getMaltaCrystalId())
                        .quantity(48.0)
                        .build();
                recipeService.createRecipe(maltaCrystalRecipe);
                break;

            case "MACERACION":
                RecipeCreateDTO aguaRecipe = RecipeCreateDTO.builder()
                        .productPhaseId(phaseId)
                        .materialId(materialDataLoaderService.getAguaId())
                        .quantity(955.0)
                        .build();
                recipeService.createRecipe(aguaRecipe);
                break;

            case "FILTRACION":
                RecipeCreateDTO clarificanteRecipe = RecipeCreateDTO.builder()
                        .productPhaseId(phaseId)
                        .materialId(materialDataLoaderService.getClarificanteId())
                        .quantity(2.0)
                        .build();
                recipeService.createRecipe(clarificanteRecipe);
                break;

            case "COCCION":
                RecipeCreateDTO lupuloRecipe = RecipeCreateDTO.builder()
                        .productPhaseId(phaseId)
                        .materialId(materialDataLoaderService.getLupuloCitraId())
                        .quantity(6.0)
                        .build();
                RecipeCreateDTO aguaRecipe2 = RecipeCreateDTO.builder()
                        .productPhaseId(phaseId)
                        .materialId(materialDataLoaderService.getAguaId())
                        .quantity(50.0)
                        .build();
                recipeService.createRecipe(lupuloRecipe);
                recipeService.createRecipe(aguaRecipe2);
                break;

            case "FERMENTACION":
                RecipeCreateDTO levaduraRecipe = RecipeCreateDTO.builder()
                        .productPhaseId(phaseId)
                        .materialId(materialDataLoaderService.getLevaduraAleId())
                        .quantity(1.5)
                        .build();
                recipeService.createRecipe(levaduraRecipe);
                break;

            case "MADURACION":
            case "GASIFICACION":
                RecipeCreateDTO co2Recipe = RecipeCreateDTO.builder()
                        .productPhaseId(phaseId)
                        .materialId(materialDataLoaderService.getCo2Id())
                        .quantity(12.0)
                        .build();
                recipeService.createRecipe(co2Recipe);
                break;
        }
    }

    private void createPaleSinAlcoholRecipes(String phaseName, Long phaseId) {
        switch (phaseName) {
            case "MOLIENDA":
                RecipeCreateDTO maltaRecipe = RecipeCreateDTO.builder()
                        .productPhaseId(phaseId)
                        .materialId(materialDataLoaderService.getMaltaPaleId())
                        .quantity(180.0)
                        .build();
                recipeService.createRecipe(maltaRecipe);

                RecipeCreateDTO maltaCrystalRecipe = RecipeCreateDTO.builder()
                        .productPhaseId(phaseId)
                        .materialId(materialDataLoaderService.getMaltaCrystalId())
                        .quantity(43.0)
                        .build();
                recipeService.createRecipe(maltaCrystalRecipe);
                break;

            case "MACERACION":
                RecipeCreateDTO aguaRecipe = RecipeCreateDTO.builder()
                        .productPhaseId(phaseId)
                        .materialId(materialDataLoaderService.getAguaId())
                        .quantity(927.0)
                        .build();
                recipeService.createRecipe(aguaRecipe);
                break;

            case "FILTRACION":
                RecipeCreateDTO clarificanteRecipe = RecipeCreateDTO.builder()
                        .productPhaseId(phaseId)
                        .materialId(materialDataLoaderService.getClarificanteId())
                        .quantity(2.0)
                        .build();
                recipeService.createRecipe(clarificanteRecipe);
                break;

            case "COCCION":
                RecipeCreateDTO lupuloRecipe = RecipeCreateDTO.builder()
                        .productPhaseId(phaseId)
                        .materialId(materialDataLoaderService.getLupuloCitraId())
                        .quantity(6.0)
                        .build();
                RecipeCreateDTO aguaRecipe2 = RecipeCreateDTO.builder()
                        .productPhaseId(phaseId)
                        .materialId(materialDataLoaderService.getAguaId())
                        .quantity(50.0)
                        .build();
                recipeService.createRecipe(lupuloRecipe);
                recipeService.createRecipe(aguaRecipe2);
                break;

            case "FERMENTACION":
                RecipeCreateDTO levaduraRecipe = RecipeCreateDTO.builder()
                        .productPhaseId(phaseId)
                        .materialId(materialDataLoaderService.getLevaduraAleId())
                        .quantity(1.5)
                        .build();
                recipeService.createRecipe(levaduraRecipe);
                break;

            case "DESALCOHOLIZACION":
                RecipeCreateDTO adsorbenteRecipe = RecipeCreateDTO.builder()
                        .productPhaseId(phaseId)
                        .materialId(materialDataLoaderService.getAdsorbenteId())
                        .quantity(8.0)
                        .build();
                recipeService.createRecipe(adsorbenteRecipe);
                break;

            case "MADURACION":
            case "GASIFICACION":
                RecipeCreateDTO co2Recipe = RecipeCreateDTO.builder()
                        .productPhaseId(phaseId)
                        .materialId(materialDataLoaderService.getCo2Id())
                        .quantity(12.0)
                        .build();
                recipeService.createRecipe(co2Recipe);
                break;
        }
    }
}

