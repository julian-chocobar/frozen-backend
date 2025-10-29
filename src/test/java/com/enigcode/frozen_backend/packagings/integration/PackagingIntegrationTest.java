package com.enigcode.frozen_backend.packagings.integration;

import com.enigcode.frozen_backend.materials.model.Material;
import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import com.enigcode.frozen_backend.materials.repository.MaterialRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class PackagingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MaterialRepository materialRepository;

    private static final String USER = "test";
    private static final String PASS = "test";

    private Long envaseMaterialId;

    @BeforeEach
    void setUp() {
        // Ensure at least one ENVASE material exists for FK constraint
        Material m = Material.builder()
                .code("ENV-001")
                .name("Botella 1L")
                .type(MaterialType.ENVASE)
                .supplier("ACME")
                .value(10.0)
                .stock(100.0)
                .reservedStock(0.0)
                .unitMeasurement(UnitMeasurement.UNIDAD)
                .threshold(5.0)
                .isActive(true)
                .creationDate(OffsetDateTime.now())
                .build();
        envaseMaterialId = materialRepository.saveAndFlush(m).getId();
    }

    @Test
    @DisplayName("Packaging happy path: create -> list -> get -> update -> toggle-active")
    void packagingCrudHappyPath() throws Exception {
        // 1) Create packaging
        String createJson = "{" +
                "\"name\":\"Pack Botella 6x1L\"," +
                "\"materialId\":" + envaseMaterialId + "," +
                "\"unitMeasurement\":\"KG\"," +
                "\"quantity\":2.5" +
                "}";

        String created = mockMvc.perform(post("/packagings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Pack Botella 6x1L"))
                .andExpect(jsonPath("$.unitMeasurement").value("KG"))
                .andExpect(jsonPath("$.quantity").value(2.5))
                .andExpect(jsonPath("$.isActive").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(created).get("id").asLong();

        // 2) List packagings
        mockMvc.perform(get("/packagings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        // 3) Get by id
        mockMvc.perform(get("/packagings/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        // 4) Update packaging
        String updateJson = "{" +
                "\"name\":\"Pack Botella 6x1L Premium\"," +
                "\"unitMeasurement\":\"LT\"," +
                "\"quantity\":3.0" +
                "}";

        mockMvc.perform(patch("/packagings/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pack Botella 6x1L Premium"))
                .andExpect(jsonPath("$.unitMeasurement").value("LT"))
                .andExpect(jsonPath("$.quantity").value(3.0));

        // 5) Toggle active
        mockMvc.perform(patch("/packagings/{id}/toggle-active", id)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));

        // 6) Simple list of active packagings (should exclude toggled one if only item)
        mockMvc.perform(get("/packagings/id-name-list")
                        .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}

