package es.deusto.spq.deustocrai.facade;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StopWatch;
import org.junit.jupiter.api.Tag;

@Tag("Rendimiento")
@SpringBootTest
@AutoConfigureMockMvc
public class MaterialControllerPerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Rendimiento: 60 peticiones GET /api/materiales?buscar=Portatil")
    public void testBuscarMaterialPerformance() throws Exception {
        final int NUM_PETICIONES = 60;
        final long UMBRAL_MS = 1200; // 1.2 segundos

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // Lanzamos las 60 peticiones consecutivas
        for (int i = 0; i < NUM_PETICIONES; i++) {
            mockMvc.perform(get("/api/materiales").param("buscar", "Portatil"))
                   .andExpect(status().isOk());
        }

        stopWatch.stop();
        long tiempoTotalMs = stopWatch.getTotalTimeMillis();

        System.out.println("====== RESULTADO DE RENDIMIENTO (MATERIALES) ======");
        System.out.println("Endpoint   : GET /api/materiales?buscar=Portatil");
        System.out.println("Peticiones : " + NUM_PETICIONES);
        System.out.println("Total      : " + tiempoTotalMs + " ms");
        System.out.printf ("Media      : %.2f ms/peticion%n", tiempoTotalMs / (double) NUM_PETICIONES);
        System.out.println("===================================================");

        assertTrue(tiempoTotalMs < UMBRAL_MS,
            "La búsqueda de materiales fue demasiado lenta. Tiempo total: " + tiempoTotalMs + " ms");
    }
}