package es.deusto.spq.deustocrai.facade;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.deusto.spq.deustocrai.dto.CredentialsDTO;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Debería retornar OK y un token cuando las credenciales son correctas")
    public void testLoginSuccess() throws Exception {
        // 1. Preparar las credenciales (deben coincidir con las creadas en DataInitializer)
        CredentialsDTO loginRequest = new CredentialsDTO();
        loginRequest.setEmail("1"); // Ejemplo de email según tu test previo
        loginRequest.setPassword("1");

        // 2. Ejecutar la petición POST al endpoint /auth/login
        // Nota: Asegúrate de que el prefijo /api/ esté configurado o quítalo si no es necesario
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                // 3. Validar que la respuesta sea 200 OK
                .andExpect(status().isOk())
                // 4. Validar que el cuerpo contenga el token generado
                .andExpect(content().string(notNullValue()));
    }

    @Test
    @DisplayName("Debería retornar 401 Unauthorized cuando la contraseña es incorrecta")
    public void testLoginFailure() throws Exception {
        CredentialsDTO wrongRequest = new CredentialsDTO();
        wrongRequest.setEmail("1");
        wrongRequest.setPassword("password-incorrecta");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongRequest)))
                // Validar que el servicio deniega el acceso con 401
                .andExpect(status().isUnauthorized());
    }
}