package com.enigcode.frozen_backend.production_materials.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.materials.repository.MaterialRepository;
import com.enigcode.frozen_backend.packagings.model.Packaging;
import com.enigcode.frozen_backend.packagings.repository.PackagingRepository;
import com.enigcode.frozen_backend.products.DTO.ProductCreateDTO;
import com.enigcode.frozen_backend.products.model.Product;
import com.enigcode.frozen_backend.products.repository.ProductRepository;
import com.enigcode.frozen_backend.products.service.ProductService;
import com.enigcode.frozen_backend.product_phases.model.ProductPhase;
import com.enigcode.frozen_backend.recipes.model.Recipe;
import com.enigcode.frozen_backend.recipes.repository.RecipeRepository;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderCreateDTO;
import com.enigcode.frozen_backend.production_orders.Service.ProductionOrderService;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderResponseDTO;
import com.enigcode.frozen_backend.production_materials.repository.ProductionMaterialRepository;
import com.enigcode.frozen_backend.users.model.User;
import com.enigcode.frozen_backend.users.model.Role;
import com.enigcode.frozen_backend.users.repository.UserRepository;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductionMaterialIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductService productService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private MaterialRepository materialRepository;
    @Autowired
    private PackagingRepository packagingRepository;
    @Autowired
    private RecipeRepository recipeRepository;
    @Autowired
    private ProductionOrderService productionOrderService;
    @Autowired
    private ProductionMaterialRepository productionMaterialRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    @WithMockUser
    void getProductionMaterial_notFound_returns404() throws Exception {
        mockMvc.perform(get("/production-materials/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getProductionMaterialByPhase_emptyOrNotFound_returns200Or404() throws Exception {
        try {
            mockMvc.perform(get("/production-materials/by-production-phase/999999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        } catch (AssertionError ae) {
            mockMvc.perform(get("/production-materials/by-production-phase/999999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    @WithMockUser
    void getProductionMaterialByBatch_emptyOrNotFound_returns200Or404() throws Exception {
        try {
            mockMvc.perform(get("/production-materials/by-batch/999999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        } catch (AssertionError ae) {
            mockMvc.perform(get("/production-materials/by-batch/999999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Test
    @WithMockUser(username = "user")
    void createProductionOrder_generatesProductionMaterials_andEndpointsReturnData() throws Exception {
    // 0. Crear usuario mock en BD para evitar UsernameNotFoundException
    OffsetDateTime now = OffsetDateTime.now();
    userRepository.saveAndFlush(User.builder()
        .username("user")
        .password("password")
        .name("Test User")
        .roles(java.util.Set.of(Role.ADMIN))
        .enabled(true)
        .accountNonExpired(true)
        .accountNonLocked(true)
        .credentialsNonExpired(true)
        .build());
    
    // 1. Crear producto base
    ProductCreateDTO pc = ProductCreateDTO.builder()
        .name("Cerveza Rubia Test")
        .isAlcoholic(true)
        .standardQuantity(100.0)
        .unitMeasurement(UnitMeasurement.KG)
        .build();
    Long productId = productService.createProduct(pc).getId();
    Product product = productRepository.findById(productId).orElseThrow();

    // Marcar fases como ready para permitir orden de producción.
    // IMPORTANTE: No llamar a save/merge explícito aquí porque la lista de phases
    // fue creada como una lista inmutable (stream().toList()) en la capa de producción.
    // Al hacer merge Hibernate intenta limpiar la colección y falla con UnsupportedOperationException.
    // Como el entity está gestionado dentro de la transacción del test, basta con mutar los objetos
    // y marcar el producto como ready: el contexto de persistencia lo sincronizará automáticamente
    // antes de ejecutar la orden de producción.
    product.getPhases().forEach(phase -> {
        phase.setIsReady(Boolean.TRUE);
        phase.setEstimatedHours(2.0); // evitar NPE en cálculo de fechas (DateUtil)
        phase.setInput(100.0); // evitar NPE en cálculo de batch phases
        phase.setOutput(95.0); // evitar NPE en cálculo de batch phases
        phase.setOutputUnit(UnitMeasurement.KG);
    });
    product.markAsReady();

    // 2. Crear materiales necesarios (packaging, etiquetado y uno de receta)
    Material packagingMat = materialRepository.saveAndFlush(Material.builder()
        .code("PKG-TST")
        .name("Botella")
        .type(MaterialType.ENVASE)
        .supplier("Proveedor")
        .value(5.0)
        .stock(10_000.0)
        .unitMeasurement(UnitMeasurement.KG)
        .threshold(100.0)
        .creationDate(now)
        .build());
    Material labelingMat = materialRepository.saveAndFlush(Material.builder()
        .code("LBL-TST")
        .name("Etiqueta")
        .type(MaterialType.ETIQUETADO)
        .supplier("Proveedor")
        .value(1.0)
        .stock(10_000.0)
        .unitMeasurement(UnitMeasurement.KG)
        .threshold(100.0)
        .creationDate(now)
        .build());
    Material recipeMaterial = materialRepository.saveAndFlush(Material.builder()
        .code("ING-TST")
        .name("Malta Claro")
        .type(MaterialType.MALTA)
        .supplier("Proveedor")
        .value(20.0)
        .stock(5_000.0)
        .unitMeasurement(UnitMeasurement.KG)
        .threshold(200.0)
        .creationDate(now)
        .build());

    // 3. Crear packaging
    Packaging packaging = packagingRepository.saveAndFlush(Packaging.builder()
        .name("Pack 10kg")
        .packagingMaterial(packagingMat)
        .labelingMaterial(labelingMat)
        .quantity(10.0)
        .unitMeasurement(UnitMeasurement.KG)
        .creationDate(now)
        .build());

    // 4. Crear una receta para la primera fase del producto
    ProductPhase firstProductPhase = product.getPhases().get(0); // MOLIENDA
    recipeRepository.saveAndFlush(Recipe.builder()
        .productPhase(firstProductPhase)
        .material(recipeMaterial)
        .quantity(5.0)
        .creationDate(now)
        .build());

    // 5. Crear orden de producción que debe generar ProductionMaterials
    ProductionOrderCreateDTO orderDTO = ProductionOrderCreateDTO.builder()
        .productId(product.getId())
        .packagingId(packaging.getId())
        .quantity(100.0) // 100 total -> multiplier = 100 / standard(100) = 1
        .plannedDate(now)
        .build();
    ProductionOrderResponseDTO orderResponse = productionOrderService.createProductionOrder(orderDTO);
    Long batchId = orderResponse.getBatchId();
    assertThat(batchId).isNotNull();

    // 6. Verificar en el repositorio
    var productionMaterials = productionMaterialRepository.findAllByBatchId(batchId);
    assertThat(productionMaterials).isNotEmpty();
    assertThat(productionMaterials.get(0).getQuantity()).isEqualTo(5.0);
    Long productionPhaseId = productionMaterials.get(0).getProductionPhase().getId();

    // 7. Verificar endpoints por batch
    mockMvc.perform(get("/production-materials/by-batch/" + batchId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].materialId").value(recipeMaterial.getId()))
        .andExpect(jsonPath("$[0].quantity").value(5.0));

    // 8. Verificar endpoint por fase
    mockMvc.perform(get("/production-materials/by-production-phase/" + productionPhaseId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].materialId").value(recipeMaterial.getId()))
        .andExpect(jsonPath("$[0].productionPhaseId").value(productionPhaseId));
    }
}
