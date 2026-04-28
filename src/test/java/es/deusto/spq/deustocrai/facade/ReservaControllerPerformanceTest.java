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
public class ReservaControllerPerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Rendimiento: 100 peticiones GET /api/reservas/activas en menos de 2000 ms")
    public void testRendimientoListarReservasActivas() throws Exception {
        final int NUM_PETICIONES = 100;
        final long UMBRAL_MS = 2000;

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // Ejecutamos 100 peticiones GET consecutivas para comprobar la eficiencia
        for (int i = 0; i < NUM_PETICIONES; i++) {
            mockMvc.perform(get("/api/reservas/activas"))
                   .andExpect(status().isOk());
        }

        stopWatch.stop();
        long tiempoTotalMs = stopWatch.getTotalTimeMillis();

        System.out.println("====== RESULTADO DE RENDIMIENTO ======");
        System.out.println("Endpoint   : GET /api/reservas/activas");
        System.out.println("Peticiones : " + NUM_PETICIONES);
        System.out.println("Total      : " + tiempoTotalMs + " ms");
        System.out.printf ("Media      : %.2f ms/peticion%n", tiempoTotalMs / (double) NUM_PETICIONES);
        System.out.println("======================================");

        assertTrue(tiempoTotalMs < UMBRAL_MS,
            "Listar reservas activas fue demasiado lento. Tiempo total: " + tiempoTotalMs + " ms");
    }

    @Test
    @DisplayName("Rendimiento: 100 peticiones GET /api/reservas/aula/{id} en menos de 2000 ms")
    public void testRendimientoListarReservasPorAula() throws Exception {
        final int NUM_PETICIONES = 100;
        final long UMBRAL_MS = 2000;

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        for (int i = 0; i < NUM_PETICIONES; i++) {
            // Utilizamos el ID 1, ya que el DataInitializer siempre crea la "Aula 01" con ese ID por defecto
            mockMvc.perform(get("/api/reservas/aula/1"))
                   .andExpect(status().isOk());
        }

        stopWatch.stop();
        long tiempoTotalMs = stopWatch.getTotalTimeMillis();

        System.out.println("====== RESULTADO DE RENDIMIENTO ======");
        System.out.println("Endpoint   : GET /api/reservas/aula/1");
        System.out.println("Peticiones : " + NUM_PETICIONES);
        System.out.println("Total      : " + tiempoTotalMs + " ms");
        System.out.printf ("Media      : %.2f ms/peticion%n", tiempoTotalMs / (double) NUM_PETICIONES);
        System.out.println("======================================");

        assertTrue(tiempoTotalMs < UMBRAL_MS,
            "Listar reservas por aula fue demasiado lento. Tiempo total: " + tiempoTotalMs + " ms");
    }
}