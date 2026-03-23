package es.deusto.spq.deustocrai.facade;

import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.deusto.spq.deustocrai.entity.Material;

@SpringBootTest
@AutoConfigureMockMvc
public class MaterialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Debería registrar un nuevo material correctamente y devolver 201 Created")
    public void testCrearMaterialSuccess() throws Exception {
        // 1. Preparar el material de prueba
        Material nuevoMaterial = new Material();
        nuevoMaterial.setTitulo("Portátil Dell Test");
        nuevoMaterial.setNumeroSerie("SN-TEST-123");
        nuevoMaterial.setTipo("Portatil");
        nuevoMaterial.setDisponible(true);

        // 2. Ejecutar la petición POST a la API de materiales
        mockMvc.perform(post("/api/materiales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoMaterial)))
                // 3. Validar que la respuesta sea 201 Created
                .andExpect(status().isCreated())
                // 4. Validar que el objeto guardado tenga los datos correctos
                .andExpect(jsonPath("$.titulo", is("Portátil Dell Test")))
                .andExpect(jsonPath("$.numeroSerie", is("SN-TEST-123")));
    }
}