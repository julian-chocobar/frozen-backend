package com.enigcode.frozen_backend.recipes.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RecipeIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createRecipe_andGetById_happyPath() throws Exception {
        // Crear material
        String materialBody = """
        {
            "name": "Malta Integral",
            "type": "MALTA",
            "supplier": "Molino S.A.",
            "value": 80.0,
            "stock": 300.0,
            "unitMeasurement": "KG",
            "threshold": 30.0
        }
        """;
        
        MvcResult materialResult = mockMvc.perform(post("/materials")
                .contentType(MediaType.APPLICATION_JSON)
                .content(materialBody)
                .with(httpBasic("test", "test")))
                .andExpect(status().isCreated())
                .andReturn();
        
        Long materialId = objectMapper.readTree(materialResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Crear producto
                String productBody = """
                {
                        "name": "Pan Integral",
                        "isAlcoholic": false,
                        "standardQuantity": 100.0,
                        "unitMeasurement": "UNIDAD"
                }
                """;
        
        MvcResult productResult = mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productBody)
                .with(httpBasic("test", "test")))
                .andExpect(status().isCreated())
                .andReturn();
        
        Long productId = objectMapper.readTree(productResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Usar una fase existente creada automáticamente en el producto: obtener lista y elegir una válida para MALTA (MOLIENDA)
        MvcResult phasesResult = mockMvc.perform(get("/product-phases/by-product/" + productId)
                .with(httpBasic("test", "test")))
                .andExpect(status().isOk())
                .andReturn();
        var phasesJson = objectMapper.readTree(phasesResult.getResponse().getContentAsString());
        Long phaseId = null;
        for (var node : phasesJson) {
            if (node.hasNonNull("phase") && "MOLIENDA".equals(node.get("phase").asText())) {
                phaseId = node.get("id").asLong();
                break;
            }
        }
        if (phaseId == null) {
            phaseId = phasesJson.get(0).get("id").asLong();
        }

        // Crear receta
        String recipeBody = String.format("""
        {
            "productPhaseId": %d,
            "materialId": %d,
            "quantity": 5.0
        }
        """, phaseId, materialId);
        
        MvcResult recipeResult = mockMvc.perform(post("/recipes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(recipeBody)
                .with(httpBasic("test", "test")))
                .andExpect(status().isCreated())
                .andReturn();
        
        String responseBody = recipeResult.getResponse().getContentAsString();
        Long recipeId = objectMapper.readTree(responseBody).get("id").asLong();
        assertThat(recipeId).isNotNull();

        // Obtener receta por id
        MvcResult getResult = mockMvc.perform(get("/recipes/" + recipeId)
                .with(httpBasic("test", "test")))
                .andExpect(status().isOk())
                .andReturn();

        String getResponse = getResult.getResponse().getContentAsString();
        assertThat(getResponse).contains("5.0");
    }

    @Test
    void updateRecipe_happyPath() throws Exception {
        // Crear material
        String materialBody = """
        {
            "name": "Agua",
            "type": "AGUA",
            "supplier": "Ingenio S.A.",
            "value": 60.0,
            "stock": 200.0,
            "unitMeasurement": "LT",
            "threshold": 20.0
        }
        """;
        
        MvcResult materialResult = mockMvc.perform(post("/materials")
                .contentType(MediaType.APPLICATION_JSON)
                .content(materialBody)
                .with(httpBasic("test", "test")))
                .andExpect(status().isCreated())
                .andReturn();
        
        Long materialId = objectMapper.readTree(materialResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Crear producto
                String productBody = """
                {
                        "name": "Torta",
                        "isAlcoholic": false,
                        "standardQuantity": 1.0,
                        "unitMeasurement": "UNIDAD"
                }
                """;
        
        MvcResult productResult = mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productBody)
                .with(httpBasic("test", "test")))
                .andExpect(status().isCreated())
                .andReturn();
        
        Long productId = objectMapper.readTree(productResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Obtener una fase existente del producto y elegir una válida para AGUA (MACERACION o COCCION)
        MvcResult phasesResult = mockMvc.perform(get("/product-phases/by-product/" + productId)
                .with(httpBasic("test", "test")))
                .andExpect(status().isOk())
                .andReturn();
        var phasesJson = objectMapper.readTree(phasesResult.getResponse().getContentAsString());
        Long phaseId = null;
        for (var node : phasesJson) {
            if (node.hasNonNull("phase")) {
                String p = node.get("phase").asText();
                if ("MACERACION".equals(p) || "COCCION".equals(p)) {
                    phaseId = node.get("id").asLong();
                    break;
                }
            }
        }
        if (phaseId == null) {
            phaseId = phasesJson.get(0).get("id").asLong();
        }

        // Crear receta
        String recipeBody = String.format("""
        {
            "productPhaseId": %d,
            "materialId": %d,
            "quantity": 2.0
        }
        """, phaseId, materialId);
        
        MvcResult recipeResult = mockMvc.perform(post("/recipes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(recipeBody)
                .with(httpBasic("test", "test")))
                .andExpect(status().isCreated())
                .andReturn();
        
        Long recipeId = objectMapper.readTree(recipeResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Actualizar receta (cambiar cantidad)
        String updateBody = """
        {
            "quantity": 3.5
        }
        """;
        
        mockMvc.perform(patch("/recipes/" + recipeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody)
                .with(httpBasic("test", "test")))
                .andExpect(status().isOk());

        // Verificar cambios
        MvcResult getResult = mockMvc.perform(get("/recipes/" + recipeId)
                .with(httpBasic("test", "test")))
                .andExpect(status().isOk())
                .andReturn();
        
        String getResponse = getResult.getResponse().getContentAsString();
        assertThat(getResponse).contains("3.5");
    }

    @Test
    void getRecipesByProductPhase_happyPath() throws Exception {
        // Crear material
        String materialBody = """
        {
            "name": "Levadura",
            "type": "LEVADURA",
            "supplier": "Levaduras S.A.",
            "value": 40.0,
            "stock": 100.0,
            "unitMeasurement": "KG",
            "threshold": 10.0
        }
        """;
        
        MvcResult materialResult = mockMvc.perform(post("/materials")
                .contentType(MediaType.APPLICATION_JSON)
                .content(materialBody)
                .with(httpBasic("test", "test")))
                .andExpect(status().isCreated())
                .andReturn();
        
        Long materialId = objectMapper.readTree(materialResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Crear producto
                String productBody = """
                {
                        "name": "Pizza",
                        "isAlcoholic": false,
                        "standardQuantity": 1.0,
                        "unitMeasurement": "UNIDAD"
                }
                """;
        
        MvcResult productResult = mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productBody)
                .with(httpBasic("test", "test")))
                .andExpect(status().isCreated())
                .andReturn();
        
        Long productId = objectMapper.readTree(productResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Obtener una fase existente del producto y elegir una válida para LEVADURA (FERMENTACION)
        MvcResult phasesResult = mockMvc.perform(get("/product-phases/by-product/" + productId)
                .with(httpBasic("test", "test")))
                .andExpect(status().isOk())
                .andReturn();
        var phasesJson = objectMapper.readTree(phasesResult.getResponse().getContentAsString());
        Long phaseId = null;
        for (var node : phasesJson) {
            if (node.hasNonNull("phase") && "FERMENTACION".equals(node.get("phase").asText())) {
                phaseId = node.get("id").asLong();
                break;
            }
        }
        if (phaseId == null) {
            phaseId = phasesJson.get(0).get("id").asLong();
        }

        // Crear receta asociada a la fase
        String recipeBody = String.format("""
        {
            "productPhaseId": %d,
            "materialId": %d,
            "quantity": 0.5
        }
        """, phaseId, materialId);
        
        mockMvc.perform(post("/recipes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(recipeBody)
                .with(httpBasic("test", "test")))
                .andExpect(status().isCreated());

        // Obtener recetas por fase
        MvcResult listResult = mockMvc.perform(get("/recipes/by-product-phase/" + phaseId)
                .with(httpBasic("test", "test")))
                .andExpect(status().isOk())
                .andReturn();
        
        String listResponse = listResult.getResponse().getContentAsString();
        assertThat(listResponse).contains("0.5");
        assertThat(listResponse).contains("Levadura");
    }
}
