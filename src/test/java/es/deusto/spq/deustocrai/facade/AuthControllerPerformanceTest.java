package es.deusto.spq.deustocrai.facade;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.StopWatch;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.deusto.spq.deustocrai.dto.CredentialsDTO;
import es.deusto.spq.deustocrai.dao.UserRepository;
import es.deusto.spq.deustocrai.entity.User;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerPerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Aseguramos que existe un usuario en la base de datos para machacarlo a peticiones
        if(userRepository.findByEmail("rendimiento@deusto.es").isEmpty()) {
            User testUser = new User();
            testUser.setEmail("rendimiento@deusto.es");
            testUser.setPassword("password123");
            testUser.setNombre("Test");
            testUser.setApellidos("Rendimiento");
            testUser.setRole(User.Role.ESTUDIANTE);
            userRepository.save(testUser);
        }
    }

    @Test
    @DisplayName("Prueba de carga: 100 inicios de sesión en menos de 3 segundos")
    public void testRendimientoLoginMasivo() throws Exception {
        CredentialsDTO loginRequest = new CredentialsDTO();
        loginRequest.setEmail("rendimiento@deusto.es");
        loginRequest.setPassword("password123");

        String jsonRequest = objectMapper.writeValueAsString(loginRequest);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        int numPeticiones = 100;
        for (int i = 0; i < numPeticiones; i++) {
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
                    .andExpect(status().isOk());
        }

        stopWatch.stop();
        long tiempoTotalMs = stopWatch.getTotalTimeMillis();

        System.out.println("====== RESULTADO DE RENDIMIENTO (LOGIN) ======");
        System.out.println("Tiempo total para " + numPeticiones + " logins: " + tiempoTotalMs + " ms");
        System.out.println("Media por petición: " + (tiempoTotalMs / (double) numPeticiones) + " ms");
        System.out.println("==============================================");

        // Validamos que el sistema soporta esta carga en menos de 3000 ms (3 segundos)
        assertTrue(tiempoTotalMs < 3000, "El sistema de login fue demasiado lento. Tiempo: " + tiempoTotalMs + "ms");
    }
}