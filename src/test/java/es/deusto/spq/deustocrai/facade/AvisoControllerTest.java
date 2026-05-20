package es.deusto.spq.deustocrai.facade;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import es.deusto.spq.deustocrai.entity.Aviso;
import es.deusto.spq.deustocrai.entity.User;
import es.deusto.spq.deustocrai.service.AuthService;
import es.deusto.spq.deustocrai.service.AvisoService;

@Tag("Unitario")
@WebMvcTest(AvisoController.class)
public class AvisoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AvisoService avisoService;

    @MockitoBean
    private AuthService authService;

    private final String TOKEN_VALIDO   = "token-aviso-valido";
    private final String TOKEN_INVALIDO = "token-aviso-invalido";
    private User usuario;
    private Aviso aviso;

    @BeforeEach
    void setUp() {
        usuario = new User();
        usuario.setId(1L);
        usuario.setEmail("alumno@deusto.es");
        usuario.setRole(User.Role.ESTUDIANTE);

        aviso = new Aviso(usuario, Aviso.TipoAviso.PENALIZACION, "Penalización", "7 días");
        org.springframework.test.util.ReflectionTestUtils.setField(aviso, "id", 1L);

        when(authService.getEmpleadoByToken(TOKEN_VALIDO)).thenReturn(usuario);
        when(authService.getEmpleadoByToken(TOKEN_INVALIDO)).thenReturn(null);
    }


    @Test
    @DisplayName("GET /api/avisos: token válido → 200 OK con lista de avisos")
    void testObtenerMisAvisosOk() throws Exception {
        when(avisoService.obtenerAvisosUsuario(usuario)).thenReturn(List.of(aviso));

        mockMvc.perform(get("/api/avisos").header("Authorization", TOKEN_VALIDO))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/avisos: token inválido → 401")
    void testObtenerMisAvisosTokenInvalido() throws Exception {
        mockMvc.perform(get("/api/avisos").header("Authorization", TOKEN_INVALIDO))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/avisos: sin avisos → 200 OK lista vacía")
    void testObtenerMisAvisosVacio() throws Exception {
        when(avisoService.obtenerAvisosUsuario(usuario)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/avisos").header("Authorization", TOKEN_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }


    @Test
    @DisplayName("GET /api/avisos/no-leidos: token válido → 200 OK")
    void testObtenerNoLeidosOk() throws Exception {
        when(avisoService.obtenerAvisosNoLeidos(usuario)).thenReturn(List.of(aviso));

        mockMvc.perform(get("/api/avisos/no-leidos").header("Authorization", TOKEN_VALIDO))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/avisos/no-leidos: token inválido → 401")
    void testObtenerNoLeidosTokenInvalido() throws Exception {
        mockMvc.perform(get("/api/avisos/no-leidos").header("Authorization", TOKEN_INVALIDO))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @DisplayName("PUT /api/avisos/1/leer: token válido y aviso existe → 200 OK")
    void testMarcarComoLeidoOk() throws Exception {
        aviso.setLeido(true);
        when(avisoService.marcarComoLeido(1L)).thenReturn(aviso);

        mockMvc.perform(put("/api/avisos/1/leer").header("Authorization", TOKEN_VALIDO))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/avisos/99/leer: aviso no existe → 404")
    void testMarcarComoLeidoNoExiste() throws Exception {
        when(avisoService.marcarComoLeido(99L))
                .thenThrow(new IllegalArgumentException("Aviso no encontrado"));

        mockMvc.perform(put("/api/avisos/99/leer").header("Authorization", TOKEN_VALIDO))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/avisos/1/leer: token inválido → 401")
    void testMarcarComoLeidoTokenInvalido() throws Exception {
        mockMvc.perform(put("/api/avisos/1/leer").header("Authorization", TOKEN_INVALIDO))
                .andExpect(status().isUnauthorized());
    }
}