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
public class ValoracionControllerPerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Prueba de carga: Leer reseñas de un recurso 200 veces en menos de 2 segundos")
    public void testRendimientoLecturaResenasMasiva() throws Exception {
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        int numPeticiones = 200;
        // Hacemos peticiones al endpoint público de un recurso ficticio o existente
        for (int i = 0; i < numPeticiones; i++) {
            mockMvc.perform(get("/api/valoraciones/recurso/1"))
                    .andExpect(status().isOk());
        }

        stopWatch.stop();
        long tiempoTotalMs = stopWatch.getTotalTimeMillis();

        System.out.println("====== RESULTADO DE RENDIMIENTO (VALORACIONES) ======");
        System.out.println("Tiempo total para " + numPeticiones + " lecturas: " + tiempoTotalMs + " ms");
        System.out.println("Media por petición: " + (tiempoTotalMs / (double) numPeticiones) + " ms");
        System.out.println("=====================================================");

        // Validamos que el sistema soporta esta carga en menos de 2000 ms (2 segundos)
        assertTrue(tiempoTotalMs < 2000, 
            "El sistema de lectura de reseñas fue demasiado lento. Tiempo: " + tiempoTotalMs + "ms");
    }
}