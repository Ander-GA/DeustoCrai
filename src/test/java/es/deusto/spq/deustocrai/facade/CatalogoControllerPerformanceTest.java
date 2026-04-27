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

@SpringBootTest
@AutoConfigureMockMvc
public class CatalogoControllerPerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Rendimiento: 100 peticiones GET /api/libros en menos de 2 000 ms")
    public void testRendimientoListarLibros() throws Exception {

        final int NUM_PETICIONES = 100;
        final long UMBRAL_MS     = 2000;

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        for (int i = 0; i < NUM_PETICIONES; i++) {
            mockMvc.perform(get("/api/libros"))
                   .andExpect(status().isOk());
        }

        stopWatch.stop();
        long tiempoTotalMs = stopWatch.getTotalTimeMillis();

        System.out.println("====== RESULTADO DE RENDIMIENTO ======");
        System.out.println("Endpoint   : GET /api/libros (listar todos)");
        System.out.println("Peticiones : " + NUM_PETICIONES);
        System.out.println("Total      : " + tiempoTotalMs + " ms");
        System.out.printf ("Media      : %.2f ms/peticion%n", tiempoTotalMs / (double) NUM_PETICIONES);
        System.out.println("======================================");

        assertTrue(tiempoTotalMs < UMBRAL_MS,
            "Listar libros fue demasiado lento. Tiempo total: " + tiempoTotalMs + " ms");
    }

    @Test
    @DisplayName("Rendimiento: 100 peticiones GET /api/libros/buscar?q= en menos de 2 000 ms")
    public void testRendimientoBuscarLibros() throws Exception {

        final String[] TERMINOS  = {"java", "spring", "datos"};
        final int NUM_PETICIONES = 100;
        final long UMBRAL_MS     = 2000;

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        for (int i = 0; i < NUM_PETICIONES; i++) {
            String termino = TERMINOS[i % TERMINOS.length];
            mockMvc.perform(get("/api/libros/buscar").param("q", termino))
                   .andExpect(status().isOk());
        }

        stopWatch.stop();
        long tiempoTotalMs = stopWatch.getTotalTimeMillis();

        System.out.println("====== RESULTADO DE RENDIMIENTO ======");
        System.out.println("Endpoint   : GET /api/libros/buscar?q=");
        System.out.println("Peticiones : " + NUM_PETICIONES);
        System.out.println("Total      : " + tiempoTotalMs + " ms");
        System.out.printf ("Media      : %.2f ms/peticion%n", tiempoTotalMs / (double) NUM_PETICIONES);
        System.out.println("======================================");

        assertTrue(tiempoTotalMs < UMBRAL_MS,
            "La busqueda de libros fue demasiado lenta. Tiempo total: " + tiempoTotalMs + " ms");
    }

    @Test
    @DisplayName("Comparativa de cargas: 25 / 50 / 100 peticiones en ambos endpoints")
    public void testComparativaCargas() throws Exception {

        int[] cargas = {25, 50, 100};

        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║             COMPARATIVA DE CARGAS - CATALOGO                ║");
        System.out.println("╠══════════════╦══════════════╦══════════════╦════════════════╣");
        System.out.println("║   Endpoint   ║  Peticiones  ║ Total (ms)   ║  Media (ms/req)║");
        System.out.println("╠══════════════╬══════════════╬══════════════╬════════════════╣");

        for (int n : cargas) {
            StopWatch sw1 = new StopWatch();
            sw1.start();
            for (int i = 0; i < n; i++) {
                mockMvc.perform(get("/api/libros")).andExpect(status().isOk());
            }
            sw1.stop();
            long total1 = sw1.getTotalTimeMillis();
            System.out.printf("║ /api/libros  ║ %12d ║ %12d ║ %14.2f║%n",
                n, total1, total1 / (double) n);

            StopWatch sw2 = new StopWatch();
            sw2.start();
            for (int i = 0; i < n; i++) {
                mockMvc.perform(get("/api/libros/buscar").param("q", "java"))
                       .andExpect(status().isOk());
            }
            sw2.stop();
            long total2 = sw2.getTotalTimeMillis();
            System.out.printf("║ buscar?q=    ║ %12d ║ %12d ║ %14.2f║%n",
                n, total2, total2 / (double) n);

            System.out.println("╠══════════════╬══════════════╬══════════════╬════════════════╣");
        }

        System.out.println("╚══════════════╩══════════════╩══════════════╩════════════════╝");
    }
}