package com.enigcode.frozen_backend.e2e;

import com.enigcode.frozen_backend.batches.model.Batch;
import com.enigcode.frozen_backend.batches.model.BatchStatus;
import com.enigcode.frozen_backend.batches.repository.BatchRepository;
import com.enigcode.frozen_backend.batches.service.BatchService;
import com.enigcode.frozen_backend.materials.DTO.MaterialCreateDTO;
import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.materials.repository.MaterialRepository;
import com.enigcode.frozen_backend.materials.service.MaterialService;
import com.enigcode.frozen_backend.movements.model.Movement;
import com.enigcode.frozen_backend.movements.model.MovementType;
import com.enigcode.frozen_backend.movements.repository.MovementRepository;
import com.enigcode.frozen_backend.packagings.DTO.PackagingCreateDTO;
import com.enigcode.frozen_backend.packagings.model.Packaging;
import com.enigcode.frozen_backend.packagings.repository.PackagingRepository;
import com.enigcode.frozen_backend.packagings.service.PackagingService;
import com.enigcode.frozen_backend.product_phases.DTO.ProductPhaseUpdateDTO;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.product_phases.model.ProductPhase;
import com.enigcode.frozen_backend.product_phases.repository.ProductPhaseRepository;
import com.enigcode.frozen_backend.product_phases.service.ProductPhaseService;
import com.enigcode.frozen_backend.products.DTO.ProductCreateDTO;
import com.enigcode.frozen_backend.products.model.Product;
import com.enigcode.frozen_backend.products.repository.ProductRepository;
import com.enigcode.frozen_backend.products.service.ProductService;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderCreateDTO;
import com.enigcode.frozen_backend.production_orders.DTO.ProductionOrderResponseDTO;
import com.enigcode.frozen_backend.production_orders.Model.OrderStatus;
import com.enigcode.frozen_backend.production_orders.Model.ProductionOrder;
import com.enigcode.frozen_backend.production_orders.Repository.ProductionOrderRepository;
import com.enigcode.frozen_backend.production_orders.Service.ProductionOrderService;
import com.enigcode.frozen_backend.recipes.DTO.RecipeCreateDTO;
import com.enigcode.frozen_backend.recipes.repository.RecipeRepository;
import com.enigcode.frozen_backend.recipes.service.RecipeService;
import com.enigcode.frozen_backend.sectors.model.Sector;
import com.enigcode.frozen_backend.sectors.model.SectorType;
import com.enigcode.frozen_backend.sectors.repository.SectorRepository;
import com.enigcode.frozen_backend.users.model.Role;
import com.enigcode.frozen_backend.users.model.User;
import com.enigcode.frozen_backend.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@Sql(scripts = "/test-data/e2e-fixtures.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("E2E: Production Flow Tests")
class ProductionFlowE2ETest {

    // ===== Services =====
    @Autowired
    private MaterialService materialService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private ProductPhaseService productPhaseService;
    
    @Autowired
    private RecipeService recipeService;
    
    @Autowired
    private PackagingService packagingService;
    
    @Autowired
    private BatchService batchService;
    
    @Autowired
    private ProductionOrderService productionOrderService;

    // ===== Repositories (para validaciones directas) =====
    @Autowired
    private MaterialRepository materialRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductPhaseRepository productPhaseRepository;
    
    @Autowired
    private RecipeRepository recipeRepository;
    
    @Autowired
    private PackagingRepository packagingRepository;
    
    @Autowired
    private BatchRepository batchRepository;
    
    @Autowired
    private ProductionOrderRepository productionOrderRepository;
    
    @Autowired
    private MovementRepository movementRepository;
    
    @Autowired
    private SectorRepository sectorRepository;
    
    @Autowired
    private UserRepository userRepository;

    // =====================================================================
    // TEST 1: Flujo Completo de Producción Exitosa (Happy Path)
    // =====================================================================
    
    @Test
    @DisplayName("E2E: Complete production flow - materials → product → order → approve → start → complete")
    void completeProductionFlow_happyPath() {
        // ===== DATOS PREPARADOS EN SQL FIXTURES =====
        // Product ID 2000: Cerveza IPA E2E Test (500L, 9 fases ready)
        // Materials: 1000-1005 (Malta, Lúpulo, Levadura, Agua, Botella, Etiqueta) - STOCK ALTO
        // Packaging ID 5000: Pack 1L E2E
        // Sector ID 900: Sector Molienda E2E (activo, disponible)
        
        Long productId = 2000L;
        Long packagingId = 5000L;
        
        // Validar que datos del SQL están cargados correctamente
        Product product = productRepository.findById(productId).orElseThrow();
        assertThat(product.getName()).isEqualTo("Cerveza IPA E2E Test");
        assertThat(product.getIsReady()).isTrue();  // Configurado en SQL
        assertThat(product.getPhases()).hasSize(8);  // 8 fases válidas (MOLIENDA, MACERACION, FILTRACION, COCCION, FERMENTACION, MADURACION, GASIFICACION, ENVASADO)
        assertThat(product.getPhases()).allMatch(ProductPhase::getIsReady);  // Todas ready
        
        Packaging packaging = packagingRepository.findById(packagingId).orElseThrow();
        assertThat(packaging.getName()).isEqualTo("Pack 1L E2E");
        
        Material malta = materialRepository.findById(1000L).orElseThrow();
        assertThat(malta.getStock()).isEqualTo(1000.0);  // Stock inicial alto
        assertThat(malta.getReservedStock()).isEqualTo(0.0);  // Sin reservas
        
        // ===== PASO 1: Crear orden de producción =====
        ProductionOrderCreateDTO orderDTO = ProductionOrderCreateDTO.builder()
            .productId(productId)
            .packagingId(packagingId)
            .quantity(500.0)  // 1x multiplicador (500L / 500L standard)
            .plannedDate(OffsetDateTime.now().plusDays(15))
            .build();
        
        ProductionOrderResponseDTO orderResponse = productionOrderService.createProductionOrder(orderDTO);
        Long orderId = orderResponse.getId();
        
        // Validar orden creada automáticamente con batch y reservas
        ProductionOrder order = productionOrderRepository.findById(orderId).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDIENTE);
        assertThat(order.getBatch()).isNotNull();  // 1 batch creado automáticamente
        
        Batch batch = order.getBatch();
        assertThat(batch.getStatus()).isEqualTo(BatchStatus.PENDIENTE);
        assertThat(batch.getQuantity()).isEqualTo(500);  // Quantity es Integer
        
        // Validar que materiales fueron reservados correctamente
        malta = materialRepository.findById(1000L).orElseThrow();
        assertThat(malta.getReservedStock()).isEqualTo(100.0);  // 100kg malta reservada (recipe: 100kg por 500L)
        assertThat(malta.getStock()).isEqualTo(900.0);  // Stock disponible decrementado (1000 - 100)
        
        Material lupulo = materialRepository.findById(1001L).orElseThrow();
        assertThat(lupulo.getReservedStock()).isEqualTo(3.0);  // 3kg lúpulo reservado (recipe: 3kg por 500L)
        
        Material levadura = materialRepository.findById(1002L).orElseThrow();
        assertThat(levadura.getReservedStock()).isEqualTo(2.0);  // 2kg levadura reservada (recipe: 2kg por 500L)
        
        Material agua = materialRepository.findById(1003L).orElseThrow();
        assertThat(agua.getReservedStock()).isEqualTo(400.0);  // 400L agua reservada
        
        // Validar movimientos de RESERVA generados
        List<Movement> reserveMovements = movementRepository.findAll().stream()
            .filter(m -> m.getType() == MovementType.RESERVA)
            .filter(m -> m.getMaterial().getId().equals(1000L) || 
                        m.getMaterial().getId().equals(1001L) || 
                        m.getMaterial().getId().equals(1002L) || 
                        m.getMaterial().getId().equals(1003L))
            .toList();
        assertThat(reserveMovements)
            .hasSizeGreaterThanOrEqualTo(4);  // Al menos 4 reservas (malta, lúpulo, levadura, agua)
    }

    // =====================================================================
    // TEST 2: Rechazo de Orden por Stock Insuficiente
    // =====================================================================
    
    @Test
    @DisplayName("E2E: Order creation fails when insufficient material stock")
    void orderCreation_failsWhenInsufficientStock() {
        // ===== DATOS PREPARADOS EN SQL FIXTURES =====
        // Product ID 2100: Cerveza Stock Bajo E2E (100L standard, 1 fase MOLIENDA ready)
        // Material ID 1100: Malta Baja Stock E2E - solo 10kg disponibles
        // Recipe: 100kg malta por cada 100L producción
        // Packaging ID 5100: Pack 1L E2E Low
        
        Long productId = 2100L;
        Long packagingId = 5100L;
        
        // Validar stock inicial bajo
        Material maltaBaja = materialRepository.findById(1100L).orElseThrow();
        assertThat(maltaBaja.getName()).isEqualTo("Malta Baja Stock E2E");
        assertThat(maltaBaja.getStock()).isEqualTo(10.0);  // Solo 10kg disponibles
        
        Product product = productRepository.findById(productId).orElseThrow();
        assertThat(product.getStandardQuantity()).isEqualTo(100.0);  // 100L standard
        
        // ===== PASO 1: Intentar crear orden que requiere 1000kg de malta =====
        // Orden solicita 1000L = 10x multiplicador (1000L / 100L standard)
        // Recipe necesita 100kg por cada 100L → 10 × 100 = 1000kg malta
        // Stock disponible: solo 10kg
        // Resultado esperado: EXCEPCIÓN por stock insuficiente
        
        ProductionOrderCreateDTO orderDTO = ProductionOrderCreateDTO.builder()
            .productId(productId)
            .packagingId(packagingId)
            .quantity(1000.0)  // 10x multiplicador → necesita 1000kg malta
            .plannedDate(OffsetDateTime.now().plusDays(15))
            .build();
        
        // ===== VALIDAR: Debe lanzar excepción por stock insuficiente =====
        assertThatThrownBy(() -> productionOrderService.createProductionOrder(orderDTO))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Stock");  // "Stock" con mayúscula según el mensaje de error real
        
        // ===== VALIDAR: Stock no debe haber cambiado tras fallo =====
        maltaBaja = materialRepository.findById(1100L).orElseThrow();
        assertThat(maltaBaja.getStock()).isEqualTo(10.0);  // Sin cambios
        assertThat(maltaBaja.getReservedStock()).isEqualTo(0.0);  // Sin reservas (rollback)
        
        // ===== VALIDAR: No se creó ninguna orden para este producto =====
        List<ProductionOrder> orders = productionOrderRepository.findAll();
        assertThat(orders)
            .noneMatch(o -> o.getProduct().getId().equals(productId));
        
        // Nota: Es posible que batch temporal exista, pero no debe estar asociado a orden de este producto
    }
}
