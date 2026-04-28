package es.deusto.spq.deustocrai.facade;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.deusto.spq.deustocrai.dto.CredentialsDTO;
import es.deusto.spq.deustocrai.dto.CreateUserDTO;
import es.deusto.spq.deustocrai.dao.UserRepository;
import es.deusto.spq.deustocrai.entity.User;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Preparamos un usuario en la BD para que los tests de login funcionen
        // sin depender del DataInitializer
        if(userRepository.findByEmail("testlogin@deusto.es").isEmpty()) {
            User testUser = new User();
            testUser.setEmail("testlogin@deusto.es");
            testUser.setPassword("password123");
            testUser.setNombre("Test");
            testUser.setApellidos("Login");
            testUser.setRole(User.Role.ESTUDIANTE);
            userRepository.save(testUser);
        }
    }

    @Test
    @DisplayName("Debería retornar OK y un token cuando las credenciales son correctas")
    public void testLoginSuccess() throws Exception {
        CredentialsDTO loginRequest = new CredentialsDTO();
        loginRequest.setEmail("testlogin@deusto.es"); 
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(notNullValue()));
    }

    @Test
    @DisplayName("Debería retornar 401 Unauthorized cuando la contraseña es incorrecta")
    public void testLoginFailure() throws Exception {
        CredentialsDTO wrongRequest = new CredentialsDTO();
        wrongRequest.setEmail("testlogin@deusto.es");
        wrongRequest.setPassword("contrasena-falsa");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Debería registrar un usuario correctamente y devolver 201 Created")
    public void testRegisterSuccess() throws Exception {
        // CORRECCIÓN: Usamos CreateUserDTO en lugar de la entidad User
        CreateUserDTO newUser = new CreateUserDTO();
        newUser.setEmail("nuevo-registro@deusto.es");
        newUser.setPassword("12345");
        newUser.setNombre("Nuevo");
        newUser.setApellidos("Usuario");
        // Suponiendo que tu DTO usa este enumerado
        newUser.setRole(CreateUserDTO.Role.ESTUDIANTE); 

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Usuario registrado con éxito"));
    }

    @Test
    @DisplayName("Debería fallar al registrar un usuario con un email que ya existe")
    public void testRegisterFailureDuplicateEmail() throws Exception {
        // Usamos el email que hemos creado en el BeforeEach para forzar el fallo
        CreateUserDTO duplicateUser = new CreateUserDTO();
        duplicateUser.setEmail("testlogin@deusto.es"); 
        duplicateUser.setPassword("cualquier-password");
        duplicateUser.setNombre("Duplicado");
        duplicateUser.setApellidos("Prueba");
        duplicateUser.setRole(CreateUserDTO.Role.ESTUDIANTE);

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El email ya está en uso"));
    }
}