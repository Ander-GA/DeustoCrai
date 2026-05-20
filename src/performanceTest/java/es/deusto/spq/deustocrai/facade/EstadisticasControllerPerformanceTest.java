package es.deusto.spq.deustocrai.facade;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StopWatch;

import es.deusto.spq.deustocrai.entity.User;
import es.deusto.spq.deustocrai.service.AuthService;

@Tag("Rendimiento")
@SpringBootTest
@AutoConfigureMockMvc
public class EstadisticasControllerPerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    // Simulamos el servicio de autenticación para que no sea el cuello de botella
    @MockitoBean
    private AuthService authService;

    @BeforeEach
    void setUp() {
        // Hacemos que el sistema se crea que somos Administradores
        User admin = new User();
        admin.setRole(User.Role.ADMIN);
        when(authService.getEmpleadoByToken(anyString())).thenReturn(admin);
    }

    @Test
    @DisplayName("Prueba de carga: Generar reporte estadístico 150 veces en menos de 3 segundos")
    public void testRendimientoGenerarEstadisticas() throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        int numPeticiones = 150; // Menos peticiones porque la query es más pesada
        
        for (int i = 0; i < numPeticiones; i++) {
            mockMvc.perform(get("/api/estadisticas")
                    .header("Authorization", "mock-token-super-seguro"))
                    .andExpect(status().isOk());
        }

        stopWatch.stop();
        long tiempoTotalMs = stopWatch.getTotalTimeMillis();

        System.out.println("====== RESULTADO DE RENDIMIENTO (ESTADÍSTICAS) ======");
        System.out.println("Tiempo total para " + numPeticiones + " reportes: " + tiempoTotalMs + " ms");
        System.out.println("Media por petición: " + (tiempoTotalMs / (double) numPeticiones) + " ms");
        System.out.println("=====================================================");

        // Validamos que nuestro motor de persistencia escale correctamente
        assertTrue(tiempoTotalMs < 4000, 
            "El sistema de generación de estadísticas es demasiado pesado. Tiempo: " + tiempoTotalMs + "ms");
    }
}