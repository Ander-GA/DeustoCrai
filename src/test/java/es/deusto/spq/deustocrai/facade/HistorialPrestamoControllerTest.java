package es.deusto.spq.deustocrai.facade;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.deusto.spq.deustocrai.dto.CredentialsDTO;

@SpringBootTest
@AutoConfigureMockMvc
public class HistorialPrestamoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    public void login() throws Exception {
        CredentialsDTO credentials = new CredentialsDTO();
        credentials.setEmail("1");
        credentials.setPassword("1");

        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isOk())
                .andReturn();

        token = result.getResponse().getContentAsString();
    }

    @Test
    @DisplayName("Debería retornar 200 OK y una lista JSON al pedir el historial con token válido")
    public void testHistorialDevuelveOk() throws Exception {
        mockMvc.perform(get("/api/prestamos/mi-historial")
                .header("Authorization", token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Todos los préstamos del historial deben tener estado DEVUELTO")
    public void testHistorialSoloContieneDevueltos() throws Exception {
        mockMvc.perform(get("/api/prestamos/mi-historial")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].estado", everyItem(is("DEVUELTO"))));
    }

    @Test
    @DisplayName("Debería retornar 401 Unauthorized si no se proporciona token")
    public void testHistorialSinTokenDevuelveUnauthorized() throws Exception {
        mockMvc.perform(get("/api/prestamos/mi-historial"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Debería retornar 401 Unauthorized si el token es inválido")
    public void testHistorialConTokenInvalidoDevuelveUnauthorized() throws Exception {
        mockMvc.perform(get("/api/prestamos/mi-historial")
                .header("Authorization", "token-inventado-invalido"))
                .andExpect(status().isUnauthorized());
    }
}