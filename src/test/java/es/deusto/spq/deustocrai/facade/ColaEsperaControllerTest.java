package es.deusto.spq.deustocrai.facade;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import es.deusto.spq.deustocrai.dao.ColaEsperaRepository;
import es.deusto.spq.deustocrai.entity.ColaEspera;
import es.deusto.spq.deustocrai.entity.User;
import es.deusto.spq.deustocrai.service.AuthService;
import es.deusto.spq.deustocrai.service.ColaEsperaService;

@Tag("Unitario")
@WebMvcTest(ColaEsperaController.class)
public class ColaEsperaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ColaEsperaService colaEsperaService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private ColaEsperaRepository colaEsperaRepository;

    private User usuarioMock;
    private ColaEspera colaMock;

    @BeforeEach
    void setUp() {
        usuarioMock = new User();
        ReflectionTestUtils.setField(usuarioMock, "id", 1L);
        usuarioMock.setEmail("estudiante@opendeusto.es");
        usuarioMock.setRole(User.Role.ESTUDIANTE);

        colaMock = new ColaEspera();
        ReflectionTestUtils.setField(colaMock, "id", 100L);
    }

    // ========================================================================
    // POST /api/cola-espera/recurso/{recursoId}
    // ========================================================================
    @Test
    @DisplayName("Apuntarse a cola exitosamente retorna 201 CREATED")
    public void testApuntarseAColaExito() throws Exception {
        when(authService.getEmpleadoByToken("token-valido")).thenReturn(usuarioMock);
        when(colaEsperaService.apuntarseACola(eq(usuarioMock), eq(10L))).thenReturn(colaMock);

        mockMvc.perform(post("/api/cola-espera/recurso/10")
                .header("Authorization", "token-valido"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(100)));
    }

    @Test
    @DisplayName("Apuntarse a cola sin token retorna 401 UNAUTHORIZED")
    public void testApuntarseAColaSinToken() throws Exception {
        mockMvc.perform(post("/api/cola-espera/recurso/10"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("No se ha recibido el token."));
    }

    @Test
    @DisplayName("Apuntarse a cola con token inválido retorna 401 UNAUTHORIZED")
    public void testApuntarseAColaTokenInvalido() throws Exception {
        when(authService.getEmpleadoByToken("token-invalido")).thenReturn(null);

        mockMvc.perform(post("/api/cola-espera/recurso/10")
                .header("Authorization", "token-invalido"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Token inválido o sesión caducada."));
    }

    @Test
    @DisplayName("Apuntarse a cola de recurso inexistente retorna 404 NOT FOUND")
    public void testApuntarseAColaNoEncontrado() throws Exception {
        when(authService.getEmpleadoByToken("token-valido")).thenReturn(usuarioMock);
        when(colaEsperaService.apuntarseACola(any(User.class), anyLong()))
                .thenThrow(new IllegalArgumentException("Recurso no encontrado"));

        mockMvc.perform(post("/api/cola-espera/recurso/999")
                .header("Authorization", "token-valido"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Recurso no encontrado"));
    }

    @Test
    @DisplayName("Apuntarse a cola con conflicto (ej. ya está en la cola) retorna 409 CONFLICT")
    public void testApuntarseAColaConflicto() throws Exception {
        when(authService.getEmpleadoByToken("token-valido")).thenReturn(usuarioMock);
        when(colaEsperaService.apuntarseACola(any(User.class), anyLong()))
                .thenThrow(new IllegalStateException("Ya estás en la cola de este recurso"));

        mockMvc.perform(post("/api/cola-espera/recurso/10")
                .header("Authorization", "token-valido"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Ya estás en la cola de este recurso"));
    }

    // ========================================================================
    // GET /api/cola-espera/recurso/{recursoId}
    // ========================================================================
    @Test
    @DisplayName("Obtener cola activa retorna 200 OK y la lista de colas")
    public void testObtenerColaExito() throws Exception {
        when(colaEsperaService.obtenerColaActiva(10L)).thenReturn(Arrays.asList(colaMock));

        mockMvc.perform(get("/api/cola-espera/recurso/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(100)));
    }

    // ========================================================================
    // DELETE /api/cola-espera/{colaId}
    // ========================================================================
    @Test
    @DisplayName("Salir de la cola exitosamente retorna 200 OK")
    public void testSalirDeLaColaExito() throws Exception {
        when(colaEsperaRepository.findById(100L)).thenReturn(Optional.of(colaMock));
        doNothing().when(colaEsperaRepository).deleteById(100L);

        mockMvc.perform(delete("/api/cola-espera/100"))
                .andExpect(status().isOk())
                .andExpect(content().string("Has salido de la cola correctamente"));
    }

    @Test
    @DisplayName("Salir de una cola que no existe retorna 404 NOT FOUND")
    public void testSalirDeLaColaNoEncontrado() throws Exception {
        when(colaEsperaRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/cola-espera/999"))
                .andExpect(status().isNotFound());
    }

    // ========================================================================
    // GET /api/cola-espera/recurso/{recursoId}/posicion
    // ========================================================================
    @Test
    @DisplayName("Obtener posición en la cola retorna 200 OK y el número")
    public void testObtenerPosicionExito() throws Exception {
        when(authService.getEmpleadoByToken("token-valido")).thenReturn(usuarioMock);
        when(colaEsperaService.obtenerPosicion(10L, usuarioMock.getId())).thenReturn(2);

        mockMvc.perform(get("/api/cola-espera/recurso/10/posicion")
                .header("Authorization", "token-valido"))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));
    }

    @Test
    @DisplayName("Obtener posición con token inválido retorna 401 UNAUTHORIZED")
    public void testObtenerPosicionTokenInvalido() throws Exception {
        when(authService.getEmpleadoByToken("token-invalido")).thenReturn(null);

        mockMvc.perform(get("/api/cola-espera/recurso/10/posicion")
                .header("Authorization", "token-invalido"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Token inválido"));
    }
}