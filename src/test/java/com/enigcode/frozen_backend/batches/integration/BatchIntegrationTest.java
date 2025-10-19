package com.enigcode.frozen_backend.batches.integration;

import com.enigcode.frozen_backend.batches.model.Batch;
import com.enigcode.frozen_backend.batches.model.BatchStatus;
import com.enigcode.frozen_backend.batches.repository.BatchRepository;
import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.materials.repository.MaterialRepository;
import com.enigcode.frozen_backend.packagings.model.Packaging;
import com.enigcode.frozen_backend.packagings.repository.PackagingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Test
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
                .material(material)
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
        MvcResult result = mockMvc.perform(get("/batches/" + batch.getId())
                .with(httpBasic("test", "test")))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertThat(response).contains("BATCH-TEST-001");
        assertThat(response).contains("PENDIENTE");
        assertThat(response).contains("50");
    }

    @Test
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
                .material(material)
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
                .param("size", "10")
                .with(httpBasic("test", "test")))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertThat(response).contains("BATCH-TEST-002");
        assertThat(response).contains("BATCH-TEST-003");
    }

    @Test
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
                .material(material)
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
                .param("size", "3")
                .with(httpBasic("test", "test")))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertThat(response).contains("totalPages");
        assertThat(response).contains("totalItems");
        assertThat(response).contains("hasNext");
        
        // Verificar que hay más páginas
        assertThat(objectMapper.readTree(response).get("hasNext").asBoolean()).isTrue();
    }
}
