package com.enigcode.frozen_backend.materials.integration;

import com.enigcode.frozen_backend.materials.model.MaterialType;
import com.enigcode.frozen_backend.materials.model.UnitMeasurement;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(roles = "SUPERVISOR_DE_ALMACEN")
class MaterialIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createMaterial_andGetById_happyPath() throws Exception {
        // Crear material válido
        String requestBody = """
        {
            "name": "Malta",
            "type": "MALTA",
            "supplier": "Proveedor S.A.",
            "value": 150.0,
            "stock": 100.0,
            "unitMeasurement": "KG",
            "threshold": 10.0
        }
        """;

    MvcResult result = mockMvc.perform(post("/materials")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
        .andExpect(status().isCreated())
        .andReturn();

    String responseBody = result.getResponse().getContentAsString();
    // Extraer el id del material creado de forma robusta
    Long materialId = objectMapper.readTree(responseBody).get("id").asLong();
        assertThat(materialId).isNotNull();

        // Obtener el material por id
    MvcResult getResult = mockMvc.perform(get("/materials/" + materialId))
        .andExpect(status().isOk())
        .andReturn();

        String getResponse = getResult.getResponse().getContentAsString();
            assertThat(getResponse).contains("Malta");
            assertThat(getResponse).contains("150.0");
    }

    @Test
    void listMaterials_happyPath() throws Exception {
        // Crear dos materiales
        String requestBody1 = """
        {
            "name": "Malta",
            "type": "MALTA",
            "supplier": "Proveedor S.A.",
            "value": 150.0,
            "stock": 100.0,
            "unitMeasurement": "KG",
            "threshold": 10.0
        }
        """;
            String requestBody2 = """
            {
                "name": "Lúpulo",
                "type": "LUPULO",
                "supplier": "Proveedor S.A.",
                "value": 80.0,
                "stock": 50.0,
                "unitMeasurement": "KG",
                "threshold": 5.0
            }
            """;
    mockMvc.perform(post("/materials")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody1))
        .andExpect(status().isCreated());
    mockMvc.perform(post("/materials")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody2))
        .andExpect(status().isCreated());

        // Listar materiales
    MvcResult listResult = mockMvc.perform(get("/materials"))
        .andExpect(status().isOk())
        .andReturn();
        String listResponse = listResult.getResponse().getContentAsString();
        assertThat(listResponse).contains("Malta");
        assertThat(listResponse).contains("Lúpulo");
    }

    @Test
    void updateMaterial_happyPath() throws Exception {
        // Crear material
        String requestBody = """
        {
            "name": "Malta",
            "type": "MALTA",
            "supplier": "Proveedor S.A.",
            "value": 150.0,
            "stock": 100.0,
            "unitMeasurement": "KG",
            "threshold": 10.0
        }
        """;
    MvcResult result = mockMvc.perform(post("/materials")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
        .andExpect(status().isCreated())
        .andReturn();
    Long materialId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // Actualizar material
        String updateBody = """
        {
            "name": "Malta Premium",
            "type": "MALTA",
            "supplier": "Proveedor S.A.",
            "value": 200.0,
            "stock": 200.0,
            "unitMeasurement": "KG",
            "threshold": 20.0
        }
        """;
    mockMvc.perform(patch("/materials/" + materialId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(updateBody))
        .andExpect(status().isOk());

        // Obtener y verificar cambios
    MvcResult getResult = mockMvc.perform(get("/materials/" + materialId))
        .andExpect(status().isOk())
        .andReturn();
        String getResponse = getResult.getResponse().getContentAsString();
        assertThat(getResponse).contains("Malta Premium");
        assertThat(getResponse).contains("200.0");
    }

    @Test
    void toggleMaterialActive_happyPath() throws Exception {
        // Crear material
        String requestBody = """
        {
            "name": "Malta",
            "type": "MALTA",
            "supplier": "Proveedor S.A.",
            "value": 150.0,
            "stock": 100.0,
            "unitMeasurement": "KG",
            "threshold": 10.0
        }
        """;
    MvcResult result = mockMvc.perform(post("/materials")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBody))
        .andExpect(status().isCreated())
        .andReturn();
        Long materialId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        // Toggle active material
    mockMvc.perform(patch("/materials/" + materialId + "/toggle-active"))
        .andExpect(status().isOk());

        // Verificar cambio de estado
    mockMvc.perform(get("/materials/" + materialId))
        .andExpect(status().isOk());
    }
}

