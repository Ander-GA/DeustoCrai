package es.deusto.spq.deustocrai.facade;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StopWatch;

@Tag("Rendimiento")
@SpringBootTest
@AutoConfigureMockMvc
public class AulaControllerPerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Prueba de carga: Listar salas 300 veces en menos de 2 segundos")
    public void testRendimientoListarSalas() throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        int numPeticiones = 300;
        
        for (int i = 0; i < numPeticiones; i++) {
            mockMvc.perform(get("/api/salas"))
                    .andExpect(status().isOk());
        }

        stopWatch.stop();
        long tiempoTotalMs = stopWatch.getTotalTimeMillis();

        System.out.println("====== RESULTADO DE RENDIMIENTO (AULAS) ======");
        System.out.println("Tiempo total para " + numPeticiones + " peticiones: " + tiempoTotalMs + " ms");
        System.out.println("Media por petición: " + (tiempoTotalMs / (double) numPeticiones) + " ms");
        System.out.println("==============================================");

        assertTrue(tiempoTotalMs < 2000, "El listado de aulas es muy lento. Tiempo: " + tiempoTotalMs + "ms");
    }
}