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
import es.deusto.spq.deustocrai.entity.User;

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
    @Test
    @DisplayName("Debería registrar un usuario correctamente y devolver 201 Created")
    public void testRegisterSuccess() throws Exception {
        User newUser = new User();
        newUser.setEmail("nuevo@deusto.es");
        newUser.setPassword("password123");
        newUser.setNombre("Prueba");
        newUser.setApellidos("Test");
        // Usamos el Enum que ya tienes en la entidad User
        newUser.setRole(User.Role.ESTUDIANTE); 

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Usuario registrado con éxito"));
    }

    @Test
    @DisplayName("Debería fallar al registrar un usuario con un email que ya existe")
    public void testRegisterFailureDuplicateEmail() throws Exception {
        // Usamos el email "1" que sabemos que ya existe por el DataInitializer
        User duplicateUser = new User();
        duplicateUser.setEmail("1"); 
        duplicateUser.setPassword("otra-password");
        duplicateUser.setNombre("Duplicado");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El email ya está en uso"));
    }
}