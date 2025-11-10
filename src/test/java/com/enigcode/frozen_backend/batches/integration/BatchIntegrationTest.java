package com.enigcode.frozen_backend.batches.integration;

import com.enigcode.frozen_backend.batches.model.Batch;
import com.enigcode.frozen_backend.batches.model.BatchStatus;
import com.enigcode.frozen_backend.batches.repository.BatchRepository;
import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.materials.repository.MaterialRepository;
import com.enigcode.frozen_backend.movements.model.Movement;
import com.enigcode.frozen_backend.movements.model.MovementType;
import com.enigcode.frozen_backend.movements.repository.MovementRepository;
import com.enigcode.frozen_backend.packagings.model.Packaging;
import com.enigcode.frozen_backend.packagings.repository.PackagingRepository;
import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.production_materials.model.ProductionMaterial;
import com.enigcode.frozen_backend.production_materials.repository.ProductionMaterialRepository;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhase;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhaseStatus;
import com.enigcode.frozen_backend.production_phases.repository.ProductionPhaseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BatchIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BatchRepository batchRepository;

    @Autowired
    private PackagingRepository packagingRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private ProductionPhaseRepository productionPhaseRepository;

    @Autowired
    private ProductionMaterialRepository productionMaterialRepository;

    @Autowired
    private MovementRepository movementRepository;

    // --- Tests originales ---

    @Test
    @WithMockUser(roles = "SUPERVISOR_DE_PRODUCCION")
    void getBatchById_happyPath() throws Exception {
        // Crear entidades necesarias directamente en la BD
        Material material = Material.builder()
                .name("Cartón")
                .type(MaterialType.OTROS)
                .supplier("Proveedor S.A.")
                .value(10.0)
                .stock(1000.0)
                .reservedStock(0.0)
                .unitMeasurement(UnitMeasurement.UNIDAD)
                .threshold(100.0)
                .isActive(true)
                .creationDate(OffsetDateTime.now())
                .build();
        material = materialRepository.save(material);

        Packaging packaging = Packaging.builder()
                .name("Caja 1L")
                .packagingMaterial(material)
                .quantity(1.0)
                .unitMeasurement(UnitMeasurement.UNIDAD)
                .isActive(true)
                .creationDate(OffsetDateTime.now())
                .build();
        packaging = packagingRepository.save(packaging);

        Batch batch = Batch.builder()
                .code("BATCH-TEST-001")
                .packaging(packaging)
                .status(BatchStatus.PENDIENTE)
                .quantity(50)
                .creationDate(OffsetDateTime.now())
                .plannedDate(OffsetDateTime.now().plusDays(1))
                .build();
        batch = batchRepository.save(batch);

        // Obtener batch por ID
        MvcResult result = mockMvc.perform(get("/batches/" + batch.getId()))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertThat(response).contains("BATCH-TEST-001");
        assertThat(response).contains("PENDIENTE");
        assertThat(response).contains("50");
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR_DE_PRODUCCION")
    void listBatches_happyPath() throws Exception {
        // Crear material
        Material material = Material.builder()
                .name("Plástico")
                .type(MaterialType.ENVASE)
                .supplier("Plásticos S.A.")
                .value(5.0)
                .stock(500.0)
                .reservedStock(0.0)
                .unitMeasurement(UnitMeasurement.UNIDAD)
                .threshold(50.0)
                .isActive(true)
                .creationDate(OffsetDateTime.now())
                .build();
        material = materialRepository.save(material);

        // Crear packaging
        Packaging packaging = Packaging.builder()
                .name("Pote 500ml")
                .packagingMaterial(material)
                .quantity(1.0)
                .unitMeasurement(UnitMeasurement.UNIDAD)
                .isActive(true)
                .creationDate(OffsetDateTime.now())
                .build();
        packaging = packagingRepository.save(packaging);

        // Crear dos batches
        Batch batch1 = Batch.builder()
                .code("BATCH-TEST-002")
                .packaging(packaging)
                .status(BatchStatus.PENDIENTE)
                .quantity(100)
                .creationDate(OffsetDateTime.now())
                .plannedDate(OffsetDateTime.now().plusDays(2))
                .build();
        batch1 = batchRepository.save(batch1);

        Batch batch2 = Batch.builder()
                .code("BATCH-TEST-003")
                .packaging(packaging)
                .status(BatchStatus.EN_PRODUCCION)
                .quantity(75)
                .creationDate(OffsetDateTime.now())
                .plannedDate(OffsetDateTime.now().plusDays(1))
                .startDate(OffsetDateTime.now())
                .build();
        batch2 = batchRepository.save(batch2);

        // Listar batches - Usar parámetros explícitos para evitar validaciones
        MvcResult result = mockMvc.perform(get("/batches")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertThat(response).contains("BATCH-TEST-002");
        assertThat(response).contains("BATCH-TEST-003");
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR_DE_PRODUCCION")
    void listBatches_withPagination() throws Exception {
        // Crear material y packaging
        Material material = Material.builder()
                .name("Cartón Industrial")
                .type(MaterialType.OTROS)
                .supplier("Cartonería S.A.")
                .value(20.0)
                .stock(200.0)
                .reservedStock(0.0)
                .unitMeasurement(UnitMeasurement.UNIDAD)
                .threshold(20.0)
                .isActive(true)
                .creationDate(OffsetDateTime.now())
                .build();
        material = materialRepository.save(material);

        Packaging packaging = Packaging.builder()
                .name("Balde 5L")
                .packagingMaterial(material)
                .quantity(1.0)
                .unitMeasurement(UnitMeasurement.UNIDAD)
                .isActive(true)
                .creationDate(OffsetDateTime.now())
                .build();
        packaging = packagingRepository.save(packaging);

        // Crear múltiples batches
        for (int i = 1; i <= 5; i++) {
            Batch batch = Batch.builder()
                    .code("BATCH-PAGING-" + String.format("%03d", i))
                    .packaging(packaging)
                    .status(BatchStatus.PENDIENTE)
                    .quantity(50 + i)
                    .creationDate(OffsetDateTime.now().minusDays(i))
                    .plannedDate(OffsetDateTime.now().plusDays(i))
                    .build();
            batchRepository.save(batch);
        }

        // Obtener primera página (size=3) con parámetros explícitos
        MvcResult result = mockMvc.perform(get("/batches")
                .param("page", "0")
                .param("size", "3"))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertThat(response).contains("totalPages");
        assertThat(response).contains("totalItems");
        assertThat(response).contains("hasNext");
        
        // Verificar que hay más páginas
        assertThat(objectMapper.readTree(response).get("hasNext").asBoolean()).isTrue();
    }

    // --- Nuevos tests de integración ---

    @Test
    @WithMockUser(roles = "GERENTE_DE_PLANTA")
    void cancelBatch_fullFlow_suspendsPhasesAndReturnsMaterials() throws Exception {
        // Given: Batch con fases y materiales
        Material maltaMaterial = Material.builder()
                .name("Malta Pilsen")
                .type(MaterialType.MALTA)
                .supplier("Maltería del Norte")
                .value(150.0)
                .stock(500.0)
                .reservedStock(0.0)
                .unitMeasurement(UnitMeasurement.KG)
                .threshold(50.0)
                .isActive(true)
                .creationDate(OffsetDateTime.now())
                .build();
        maltaMaterial = materialRepository.save(maltaMaterial);

        Material packagingMat = Material.builder()
                .name("Botella Vidrio")
                .type(MaterialType.ENVASE)
                .supplier("Vidriera S.A.")
                .value(5.0)
                .stock(1000.0)
                .reservedStock(0.0)
                .unitMeasurement(UnitMeasurement.UNIDAD)
                .threshold(100.0)
                .isActive(true)
                .creationDate(OffsetDateTime.now())
                .build();
        packagingMat = materialRepository.save(packagingMat);

        Packaging packaging = Packaging.builder()
                .name("Botella 500ml")
                .packagingMaterial(packagingMat)
                .quantity(1.0)
                .unitMeasurement(UnitMeasurement.UNIDAD)
                .isActive(true)
                .creationDate(OffsetDateTime.now())
                .build();
        packaging = packagingRepository.save(packaging);

        Batch batch = Batch.builder()
                .code("BATCH-CANCEL-001")
                .packaging(packaging)
                .status(BatchStatus.PENDIENTE)
                .quantity(100)
                .creationDate(OffsetDateTime.now())
                .plannedDate(OffsetDateTime.now().plusDays(1))
                .phases(new ArrayList<>())
                .build();

        ProductionPhase phase1 = ProductionPhase.builder()
                .batch(batch)
                .phase(Phase.MOLIENDA)
                .status(ProductionPhaseStatus.PENDIENTE)
                .standardInput(100.0)
                .standardOutput(95.0)
                .outputUnit(UnitMeasurement.KG)
                .build();

        ProductionPhase phase2 = ProductionPhase.builder()
                .batch(batch)
                .phase(Phase.MACERACION)
                .status(ProductionPhaseStatus.COMPLETADA)
                .standardInput(95.0)
                .standardOutput(90.0)
                .outputUnit(UnitMeasurement.KG)
                .build();

        batch.getPhases().add(phase1);
        batch.getPhases().add(phase2);
        batch = batchRepository.saveAndFlush(batch);

        // Asignar materiales a la fase pendiente
        ProductionMaterial pm = ProductionMaterial.builder()
                .productionPhase(phase1)
                .material(maltaMaterial)
                .quantity(100.0)
                .build();
        productionMaterialRepository.save(pm);

        Long initialMovementCount = movementRepository.count();

        // When: Cancelar batch
        mockMvc.perform(patch("/batches/cancel-batch/" + batch.getId())
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("gerenteplanta").roles("GERENTE_DE_PLANTA"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Then: Verificar estado y efectos
        Batch canceledBatch = batchRepository.findById(batch.getId()).orElseThrow();
        assertThat(canceledBatch.getStatus()).isEqualTo(BatchStatus.CANCELADO);

        ProductionPhase suspendedPhase = productionPhaseRepository.findById(phase1.getId()).orElseThrow();
        assertThat(suspendedPhase.getStatus()).isEqualTo(ProductionPhaseStatus.SUSPENDIDA);

        ProductionPhase completedPhase = productionPhaseRepository.findById(phase2.getId()).orElseThrow();
        assertThat(completedPhase.getStatus()).isEqualTo(ProductionPhaseStatus.COMPLETADA); // Sin cambio

        // Verificar movimiento de devolución
        Long finalMovementCount = movementRepository.count();
        assertThat(finalMovementCount).isGreaterThan(initialMovementCount);

        List<Movement> movements = movementRepository.findAll();
        Movement returnMovement = movements.stream()
                .filter(m -> m.getType() == MovementType.INGRESO && m.getReason().contains("Cancelación"))
                .findFirst()
                .orElseThrow();

        assertThat(returnMovement.getStock()).isEqualTo(100.0);
        assertThat(returnMovement.getMaterial().getId()).isEqualTo(maltaMaterial.getId());
    }

    @Test
    @WithMockUser(roles = "GERENTE_DE_PLANTA")
    void processBatchesForToday_manual_startsScheduledBatches() throws Exception {
        // Given: Este test ejecuta el endpoint pero el comportamiento depende de la fecha actual
        // y configuración del sistema, por lo que verificamos que el endpoint es accesible

        // When/Then
        mockMvc.perform(post("/batches/process-today")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Note: La lógica interna del scheduler requiere setup completo de sectores y configuración
        // Para tests más detallados se deberían usar los unit tests del servicio
    }

        @Test
        @WithMockUser(roles = "SUPERVISOR_DE_PRODUCCION")
        void getBatches_withStatusFilter_filtersCorrectly() throws Exception {
        // Given: Crear batches con diferentes estados
        Material mat = Material.builder()
                .name("Material Test")
                .type(MaterialType.OTROS)
                .supplier("Test Supplier")
                .value(10.0)
                .stock(1000.0)
                .reservedStock(0.0)
                .unitMeasurement(UnitMeasurement.UNIDAD)
                .threshold(50.0)
                .isActive(true)
                .creationDate(OffsetDateTime.now())
                .build();
        mat = materialRepository.save(mat);

        Packaging pkg = Packaging.builder()
                .name("Test Package")
                .packagingMaterial(mat)
                .quantity(1.0)
                .unitMeasurement(UnitMeasurement.UNIDAD)
                .isActive(true)
                .creationDate(OffsetDateTime.now())
                .build();
        pkg = packagingRepository.save(pkg);

        Batch pendingBatch = Batch.builder()
                .code("BATCH-FILTER-001")
                .packaging(pkg)
                .status(BatchStatus.PENDIENTE)
                .quantity(50)
                .creationDate(OffsetDateTime.now())
                .plannedDate(OffsetDateTime.now().plusDays(1))
                .build();
        batchRepository.save(pendingBatch);

        Batch productionBatch = Batch.builder()
                .code("BATCH-FILTER-002")
                .packaging(pkg)
                .status(BatchStatus.EN_PRODUCCION)
                .quantity(75)
                .creationDate(OffsetDateTime.now())
                .plannedDate(OffsetDateTime.now())
                .startDate(OffsetDateTime.now())
                .build();
        batchRepository.save(productionBatch);

        // When: Filtrar por EN_PRODUCCION
        MvcResult result = mockMvc.perform(get("/batches")
                        .param("status", "EN_PRODUCCION")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn();

        // Then: Solo debe retornar batch en producción
        String response = result.getResponse().getContentAsString();
        assertThat(response).contains("BATCH-FILTER-002");
        assertThat(response).doesNotContain("BATCH-FILTER-001"); // Filtro debería excluir PENDIENTE
    }
}
