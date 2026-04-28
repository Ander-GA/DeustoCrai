package es.deusto.spq.deustocrai.facade;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.StopWatch;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.deusto.spq.deustocrai.dto.CredentialsDTO;
import org.junit.jupiter.api.Tag;

@Tag("Rendimiento")
@SpringBootTest
@AutoConfigureMockMvc
public class PrestamoControllerPerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Prueba de carga: 100 peticiones de listar préstamos en menos de 2 segundos")
    public void testRendimientoObtenerTodosLosPrestamos() throws Exception {
        CredentialsDTO login = new CredentialsDTO();
        login.setEmail("1");
        login.setPassword("1");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();
        
        String token = loginResult.getResponse().getContentAsString();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        int numPeticiones = 100;
        for (int i = 0; i < numPeticiones; i++) {
            mockMvc.perform(get("/api/prestamos/todos")
                    .header("Authorization", token))
                    .andExpect(status().isOk());
        }

        stopWatch.stop();
        long tiempoTotalMs = stopWatch.getTotalTimeMillis();

        System.out.println("====== RESULTADO DE RENDIMIENTO ======");
        System.out.println("Tiempo total para " + numPeticiones + " peticiones: " + tiempoTotalMs + " ms");
        System.out.println("Media por petición: " + (tiempoTotalMs / (double) numPeticiones) + " ms");
        System.out.println("======================================");

        assertTrue(tiempoTotalMs < 2000, "El sistema fue demasiado lento. Tiempo: " + tiempoTotalMs + "ms");
    }
}