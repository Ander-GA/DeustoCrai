package es.deusto.spq.deustocrai.facade;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StopWatch;
import org.junit.jupiter.api.Tag;

import es.deusto.spq.deustocrai.entity.User;
import es.deusto.spq.deustocrai.service.AuthService;

@Tag("Rendimiento")
@SpringBootTest
@AutoConfigureMockMvc
public class PrestamoControllerPerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    // Simulamos la autenticación para que el test de rendimiento no dependa de la BD
    @MockBean
    private AuthService authService;

    private String validAdminToken = "token-admin-rendimiento";

    @BeforeEach
    public void setUp() {
        // Creamos un usuario BIBLIOTECARIO simulado (necesario para poder acceder a /todos)
        User adminUser = new User();
        adminUser.setId(1L);
        adminUser.setEmail("biblioteca@deusto.es");
        adminUser.setRole(User.Role.BIBLIOTECARIO);
        
        // Le decimos al mock que acepte nuestro token inventado
        when(authService.getEmpleadoByToken(validAdminToken)).thenReturn(adminUser);
    }

    @Test
    @DisplayName("Prueba de carga: 100 peticiones de listar préstamos en menos de 2 segundos")
    public void testRendimientoObtenerTodosLosPrestamos() throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        int numPeticiones = 100;
        // Hacemos las 100 peticiones pasando directamente nuestro token simulado
        for (int i = 0; i < numPeticiones; i++) {
            mockMvc.perform(get("/api/prestamos/todos")
                    .header("Authorization", validAdminToken))
                    .andExpect(status().isOk());
        }

        stopWatch.stop();
        long tiempoTotalMs = stopWatch.getTotalTimeMillis();

        System.out.println("====== RESULTADO DE RENDIMIENTO ======");
        System.out.println("Tiempo total para " + numPeticiones + " peticiones: " + tiempoTotalMs + " ms");
        System.out.println("Media por petición: " + (tiempoTotalMs / (double) numPeticiones) + " ms");
        System.out.println("======================================");

        // Verificamos que tarda menos de 2 segundos (2000 ms)
        assertTrue(tiempoTotalMs < 2000, "El sistema fue demasiado lento. Tiempo: " + tiempoTotalMs + "ms");
    }
}