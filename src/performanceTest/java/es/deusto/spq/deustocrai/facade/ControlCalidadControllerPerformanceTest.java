package es.deusto.spq.deustocrai.facade;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
public class ControlCalidadControllerPerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @BeforeEach
    void setUp() {
        // Nos autenticamos de forma simulada como Bibliotecarios para pasar el filtro de seguridad
        User biblio = new User();
        biblio.setRole(User.Role.BIBLIOTECARIO);
        when(authService.getEmpleadoByToken(anyString())).thenReturn(biblio);
    }

    @Test
    @DisplayName("Prueba de carga: Registrar 200 controles de calidad (APTO) masivos en menos de 4s")
    public void testRendimientoRegistroControlMasivo() throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        int numPeticiones = 200;
        
        for (int i = 0; i < numPeticiones; i++) {
            // Nota: Forzamos la respuesta de material no encontrado (404) a propósito ya que 
            // no queremos crear 200 materiales basura en base de datos.
            // Lo que evaluamos es la velocidad de despacho del servidor (Request Dispatcher).
            mockMvc.perform(post("/api/control-calidad")
                    .header("Authorization", "token-biblio-123")
                    .param("materialId", "99999") 
                    .param("estado", "APTO")
                    .param("observaciones", "Test de carga automático"))
                    .andExpect(status().isNotFound()); // Toleramos el 404
        }

        stopWatch.stop();
        long tiempoTotalMs = stopWatch.getTotalTimeMillis();

        System.out.println("====== RESULTADO DE RENDIMIENTO (CONTROL CALIDAD) ======");
        System.out.println("Tiempo total para " + numPeticiones + " registros: " + tiempoTotalMs + " ms");
        System.out.println("Media por petición: " + (tiempoTotalMs / (double) numPeticiones) + " ms");
        System.out.println("========================================================");

        assertTrue(tiempoTotalMs < 4000, 
            "El sistema de registro masivo de control de calidad colapsa. Tiempo: " + tiempoTotalMs + "ms");
    }
}