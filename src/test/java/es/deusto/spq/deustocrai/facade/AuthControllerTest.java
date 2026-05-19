package es.deusto.spq.deustocrai.facade;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Tag;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.deusto.spq.deustocrai.dto.CredentialsDTO;
import es.deusto.spq.deustocrai.dto.CreateUserDTO;
import es.deusto.spq.deustocrai.entity.User;
import es.deusto.spq.deustocrai.service.AuthService;

@Tag("Unitario")
@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Simulamos el servicio para aislar el controlador
    @MockitoBean
    private AuthService authService;

    // --- TESTS PARA LOGIN ---
    @Test
    @DisplayName("Login: Retorna 200 OK y token si credenciales son correctas")
    public void testLoginSuccess() throws Exception {
        CredentialsDTO loginReq = new CredentialsDTO();
        loginReq.setEmail("test@deusto.es");
        loginReq.setPassword("123");

        when(authService.login("test@deusto.es", "123")).thenReturn(Optional.of("token-simulado"));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(content().string("token-simulado"));
    }

    @Test
    @DisplayName("Login: Retorna 401 si credenciales son incorrectas")
    public void testLoginFailure() throws Exception {
        CredentialsDTO loginReq = new CredentialsDTO();
        loginReq.setEmail("test@deusto.es");
        loginReq.setPassword("mal");

        when(authService.login("test@deusto.es", "mal")).thenReturn(Optional.empty());

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized());
    }

    // --- TESTS PARA LOGOUT ---
    @Test
    @DisplayName("Logout: Retorna 204 No Content si se desloguea con éxito")
    public void testLogoutSuccess() throws Exception {
        when(authService.logout("token-valido")).thenReturn(Optional.of(true));

        mockMvc.perform(post("/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("token-valido"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Logout: Retorna 401 si el token es inválido")
    public void testLogoutFailure() throws Exception {
        when(authService.logout("token-falso")).thenReturn(Optional.empty());

        mockMvc.perform(post("/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("token-falso"))
                .andExpect(status().isUnauthorized());
    }

    // --- TESTS PARA REGISTER ---
    @Test
    @DisplayName("Register: Registra usuario correctamente y devuelve 201")
    public void testRegisterSuccess() throws Exception {
        CreateUserDTO newUser = new CreateUserDTO();
        newUser.setEmail("nuevo@deusto.es");
        newUser.setPassword("12345");
        
        when(authService.register(any(CreateUserDTO.class))).thenReturn(Optional.of(new User()));

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Usuario registrado con éxito"));
    }

    @Test
    @DisplayName("Register: Falla si email ya existe y devuelve 400")
    public void testRegisterFailureDuplicateEmail() throws Exception {
        CreateUserDTO duplicateUser = new CreateUserDTO();
        duplicateUser.setEmail("duplicado@deusto.es");
        
        when(authService.register(any(CreateUserDTO.class))).thenReturn(Optional.empty());

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("El email ya está en uso"));
    }

    // --- TESTS PARA GET CURRENT USER (/me) ---
    @Test
    @DisplayName("Me: Retorna los datos del usuario si el token es válido")
    public void testGetCurrentUserSuccess() throws Exception {
        User mockUser = new User();
        mockUser.setEmail("usuario@deusto.es");

        when(authService.getEmpleadoByToken("token-valido")).thenReturn(mockUser);

        mockMvc.perform(get("/auth/me")
                .header("Authorization", "token-valido"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Me: Retorna 401 si el token es inválido")
    public void testGetCurrentUserFailure() throws Exception {
        when(authService.getEmpleadoByToken("token-invalido")).thenReturn(null);

        mockMvc.perform(get("/auth/me")
                .header("Authorization", "token-invalido"))
                .andExpect(status().isUnauthorized());
    }
}