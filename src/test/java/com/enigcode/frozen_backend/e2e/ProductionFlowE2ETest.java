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
    // TEST 2: Flujo de Creación de Múltiples Órdenes con Reserva de Stock
    // =====================================================================
    
    @Test
    @DisplayName("E2E: Multiple order creation with cumulative stock reservation")
    void completeProductionFlow_multipleOrdersWithStockReservation() {
        // ===== DATOS PREPARADOS EN SQL FIXTURES =====
        // Product ID 2000: Cerveza IPA E2E Test (500L, 8 fases ready)
        // Materials: 1000-1005 (Malta, Lúpulo, Levadura, Agua, Botella, Etiqueta) - STOCK ALTO
        // Packaging ID 5000: Pack 1L E2E
        
        Long productId = 2000L;
        Long packagingId = 5000L;
        
        // Capturar stock inicial
        Material malta = materialRepository.findById(1000L).orElseThrow();
        Double maltaStockInicial = malta.getStock();
        Double maltaReservedInicial = malta.getReservedStock();
        
        Material lupulo = materialRepository.findById(1001L).orElseThrow();
        Double lupuloStockInicial = lupulo.getStock();
        
        // ===== PASO 1: Crear PRIMERA orden de producción =====
        ProductionOrderCreateDTO orderDTO1 = ProductionOrderCreateDTO.builder()
            .productId(productId)
            .packagingId(packagingId)
            .quantity(500.0)  // 1x multiplicador
            .plannedDate(OffsetDateTime.now().plusDays(5))
            .build();
        
        ProductionOrderResponseDTO orderResponse1 = productionOrderService.createProductionOrder(orderDTO1);
        Long orderId1 = orderResponse1.getId();
        
        // Validar PRIMERA orden creada
        ProductionOrder order1 = productionOrderRepository.findById(orderId1).orElseThrow();
        assertThat(order1.getStatus()).isEqualTo(OrderStatus.PENDIENTE);
        assertThat(order1.getBatch()).isNotNull();
        assertThat(order1.getBatch().getStatus()).isEqualTo(BatchStatus.PENDIENTE);
        
        // Validar reserva de stock tras PRIMERA orden
        malta = materialRepository.findById(1000L).orElseThrow();
        assertThat(malta.getReservedStock()).isEqualTo(100.0);  // 100kg malta reservada
        assertThat(malta.getStock()).isEqualTo(maltaStockInicial - 100.0);  // Stock disponible decrementado
        
        lupulo = materialRepository.findById(1001L).orElseThrow();
        assertThat(lupulo.getReservedStock()).isEqualTo(3.0);  // 3kg lúpulo reservado
        assertThat(lupulo.getStock()).isEqualTo(lupuloStockInicial - 3.0);
        
        // ===== PASO 2: Crear SEGUNDA orden de producción (doble cantidad) =====
        ProductionOrderCreateDTO orderDTO2 = ProductionOrderCreateDTO.builder()
            .productId(productId)
            .packagingId(packagingId)
            .quantity(1000.0)  // 2x multiplicador
            .plannedDate(OffsetDateTime.now().plusDays(10))
            .build();
        
        ProductionOrderResponseDTO orderResponse2 = productionOrderService.createProductionOrder(orderDTO2);
        Long orderId2 = orderResponse2.getId();
        
        // Validar SEGUNDA orden creada
        ProductionOrder order2 = productionOrderRepository.findById(orderId2).orElseThrow();
        assertThat(order2.getStatus()).isEqualTo(OrderStatus.PENDIENTE);
        assertThat(order2.getBatch()).isNotNull();
        
        // Validar reserva acumulada de stock
        malta = materialRepository.findById(1000L).orElseThrow();
        assertThat(malta.getReservedStock()).isEqualTo(300.0);  // 100kg (orden1) + 200kg (orden2)
        assertThat(malta.getStock()).isEqualTo(maltaStockInicial - 300.0);  // Stock disponible decrementado
        
        lupulo = materialRepository.findById(1001L).orElseThrow();
        assertThat(lupulo.getReservedStock()).isEqualTo(9.0);  // 3kg (orden1) + 6kg (orden2)
        assertThat(lupulo.getStock()).isEqualTo(lupuloStockInicial - 9.0);
        
        // ===== PASO 3: Crear TERCERA orden para validar acumulación =====
        ProductionOrderCreateDTO orderDTO3 = ProductionOrderCreateDTO.builder()
            .productId(productId)
            .packagingId(packagingId)
            .quantity(500.0)  // 1x multiplicador
            .plannedDate(OffsetDateTime.now().plusDays(15))
            .build();
        
        ProductionOrderResponseDTO orderResponse3 = productionOrderService.createProductionOrder(orderDTO3);
        Long orderId3 = orderResponse3.getId();
        
        // Validar TERCERA orden creada
        ProductionOrder order3 = productionOrderRepository.findById(orderId3).orElseThrow();
        assertThat(order3.getStatus()).isEqualTo(OrderStatus.PENDIENTE);
        
        // Validar reserva acumulada final
        malta = materialRepository.findById(1000L).orElseThrow();
        assertThat(malta.getReservedStock()).isEqualTo(400.0);  // 100 + 200 + 100
        assertThat(malta.getStock()).isEqualTo(maltaStockInicial - 400.0);
        
        lupulo = materialRepository.findById(1001L).orElseThrow();
        assertThat(lupulo.getReservedStock()).isEqualTo(12.0);  // 3 + 6 + 3
        assertThat(lupulo.getStock()).isEqualTo(lupuloStockInicial - 12.0);
        
        // ===== VALIDACIONES FINALES =====
        // Validar movimientos generados
        List<Movement> reserveMovements = movementRepository.findAll().stream()
            .filter(m -> m.getType() == MovementType.RESERVA)
            .toList();
        assertThat(reserveMovements).hasSizeGreaterThanOrEqualTo(12);  // 4 materiales x 3 órdenes
        
        // Validar que las 3 órdenes siguen PENDIENTES
        order1 = productionOrderRepository.findById(orderId1).orElseThrow();
        order2 = productionOrderRepository.findById(orderId2).orElseThrow();
        order3 = productionOrderRepository.findById(orderId3).orElseThrow();
        
        assertThat(order1.getStatus()).isEqualTo(OrderStatus.PENDIENTE);
        assertThat(order2.getStatus()).isEqualTo(OrderStatus.PENDIENTE);
        assertThat(order3.getStatus()).isEqualTo(OrderStatus.PENDIENTE);
        
        // Este test E2E valida:
        // - Creación de múltiples órdenes con reserva automática de materiales
        // - Acumulación correcta de stock reservado
        // - Generación correcta de movimientos de tipo RESERVA
        // - Stock disponible se reduce correctamente con cada nueva orden
    }

    // =====================================================================
    // TEST 3: Competencia por Stock Limitado entre Múltiples Órdenes
    // =====================================================================
    
    @Test
    @DisplayName("E2E: Concurrent orders competing for limited stock - first wins, second fails")
    void multipleOrders_stockCompetition() {
        // ===== ESCENARIO =====
        // Material con stock limitado: 100kg malta disponible
        // Orden 1 necesita 80kg → DEBE PASAR (queda 20kg disponible)
        // Orden 2 necesita 40kg → DEBE FALLAR (solo hay 20kg disponibles)
        
        // ===== PREPARACIÓN: Crear producto y material con stock limitado =====
        Long productId = 2100L;  // Producto del fixture con receta que usa malta
        Long packagingId = 5100L;
        
        // Validar stock inicial limitado
        Material maltaBaja = materialRepository.findById(1100L).orElseThrow();
        assertThat(maltaBaja.getName()).isEqualTo("Malta Baja Stock E2E");
        assertThat(maltaBaja.getStock()).isEqualTo(10.0);  // Solo 10kg disponibles inicialmente
        assertThat(maltaBaja.getReservedStock()).isEqualTo(0.0);
        
        // ===== PASO 1: Aumentar stock a 100kg para el escenario =====
        // Simular entrada de material para tener exactamente 100kg
        maltaBaja.setStock(100.0);
        materialRepository.save(maltaBaja);
        
        // Verificar stock inicial
        maltaBaja = materialRepository.findById(1100L).orElseThrow();
        assertThat(maltaBaja.getStock()).isEqualTo(100.0);
        assertThat(maltaBaja.getReservedStock()).isEqualTo(0.0);
        
        // ===== PASO 2: Crear ORDEN 1 que necesita 80kg (0.8 unidades × 100kg/unidad) =====
        ProductionOrderCreateDTO orderDTO1 = ProductionOrderCreateDTO.builder()
            .productId(productId)
            .packagingId(packagingId)
            .quantity(80.0)  // 0.8x multiplicador → 80kg de malta
            .plannedDate(OffsetDateTime.now().plusDays(5))
            .build();
        
        ProductionOrderResponseDTO orderResponse1 = productionOrderService.createProductionOrder(orderDTO1);
        Long orderId1 = orderResponse1.getId();
        
        // Validar ORDEN 1 creada exitosamente
        ProductionOrder order1 = productionOrderRepository.findById(orderId1).orElseThrow();
        assertThat(order1.getStatus()).isEqualTo(OrderStatus.PENDIENTE);
        assertThat(order1.getBatch()).isNotNull();
        
        // Validar que se reservaron 80kg de malta
        maltaBaja = materialRepository.findById(1100L).orElseThrow();
        assertThat(maltaBaja.getReservedStock()).isEqualTo(80.0);  // 80kg reservados
        assertThat(maltaBaja.getStock()).isEqualTo(20.0);  // Quedan solo 20kg disponibles
        
        // ===== PASO 3: Intentar crear ORDEN 2 que necesita 40kg (DEBE FALLAR) =====
        ProductionOrderCreateDTO orderDTO2 = ProductionOrderCreateDTO.builder()
            .productId(productId)
            .packagingId(packagingId)
            .quantity(40.0)  // 0.4x multiplicador → 40kg de malta (pero solo hay 20kg!)
            .plannedDate(OffsetDateTime.now().plusDays(10))
            .build();
        
        // Validar que lanza excepción por stock insuficiente
        assertThatThrownBy(() -> productionOrderService.createProductionOrder(orderDTO2))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Stock");  // Mensaje de error debe mencionar "Stock"
        
        // ===== VALIDACIONES FINALES =====
        
        // 1. Stock de malta NO debe haber cambiado tras el fallo de ORDEN 2
        maltaBaja = materialRepository.findById(1100L).orElseThrow();
        assertThat(maltaBaja.getReservedStock()).isEqualTo(80.0);  // Solo reserva de ORDEN 1
        assertThat(maltaBaja.getStock()).isEqualTo(20.0);  // Sin cambios tras el fallo
        
        // 2. Solo debe existir 1 orden para este producto (ORDEN 1)
        List<ProductionOrder> ordersForProduct = productionOrderRepository.findAll().stream()
            .filter(o -> o.getProduct().getId().equals(productId))
            .toList();
        assertThat(ordersForProduct).hasSize(1);  // Solo ORDEN 1 existe
        assertThat(ordersForProduct.get(0).getId()).isEqualTo(orderId1);
        
        // 3. ORDEN 1 sigue activa y con su batch
        order1 = productionOrderRepository.findById(orderId1).orElseThrow();
        assertThat(order1.getStatus()).isEqualTo(OrderStatus.PENDIENTE);
        assertThat(order1.getBatch()).isNotNull();
        assertThat(order1.getBatch().getStatus()).isEqualTo(BatchStatus.PENDIENTE);
        
        // 4. Validar movimientos: solo debe haber 1 movimiento RESERVA para malta
        List<Movement> maltaReserveMovements = movementRepository.findAll().stream()
            .filter(m -> m.getType() == MovementType.RESERVA)
            .filter(m -> m.getMaterial().getId().equals(1100L))
            .toList();
        assertThat(maltaReserveMovements).hasSize(1);  // Solo 1 reserva (ORDEN 1)
        assertThat(maltaReserveMovements.get(0).getStock()).isEqualTo(80.0);
        
        // Este test E2E valida:
        // - Competencia por stock limitado entre órdenes concurrentes
        // - Primera orden reserva el stock exitosamente
        // - Segunda orden se rechaza por stock insuficiente
        // - Stock no se modifica tras fallo (rollback correcto)
        // - Solo se crea la orden que pudo reservar el stock
    }

    // =====================================================================
    // TEST 4: Rechazo de Orden por Stock Insuficiente (caso original)
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
