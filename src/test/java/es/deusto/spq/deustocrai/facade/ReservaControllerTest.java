package es.deusto.spq.deustocrai.facade;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import es.deusto.spq.deustocrai.entity.Reserva;
import es.deusto.spq.deustocrai.entity.User;
import es.deusto.spq.deustocrai.service.AuthService;
import es.deusto.spq.deustocrai.service.ReservaService;

@Tag("Unitario")
@WebMvcTest(ReservaController.class)
public class ReservaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservaService reservaService;

    @MockitoBean
    private AuthService authService;

    private User usuarioMock;
    private Reserva reservaMock;

    @BeforeEach
    void setUp() {
        // Configuramos Jackson para que entienda LocalDateTime
        objectMapper.registerModule(new JavaTimeModule());

        usuarioMock = new User();
        ReflectionTestUtils.setField(usuarioMock, "id", 1L);
        usuarioMock.setRole(User.Role.ESTUDIANTE);

        reservaMock = new Reserva();
        ReflectionTestUtils.setField(reservaMock, "id", 100L);
        reservaMock.setFechaHoraInicio(LocalDateTime.now());
        reservaMock.setFechaHoraFin(LocalDateTime.now().plusHours(2));
    }

    // ========================================================================
    // GET /api/reservas/aula/{aulaId}
    // ========================================================================
    @Test
    @DisplayName("Listar reservas por Aula retorna 200 OK y la lista")
    public void testListarPorAula() throws Exception {
        when(reservaService.getReservasPorAula(10L)).thenReturn(Arrays.asList(reservaMock));

        mockMvc.perform(get("/api/reservas/aula/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    // ========================================================================
    // GET /api/reservas/eventos/{aulaId} (ESTO CUBRE EL LAMBDA .map)
    // ========================================================================
    @Test
    @DisplayName("Obtener eventos calendario retorna objetos mapeados correctamente")
    public void testObtenerEventosCalendario() throws Exception {
        when(reservaService.getReservasPorAula(10L)).thenReturn(Arrays.asList(reservaMock));

        mockMvc.perform(get("/api/reservas/eventos/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Ocupado")))
                .andExpect(jsonPath("$[0].color", is("#d9534f")));
    }

    // ========================================================================
    // GET /api/reservas/activas
    // ========================================================================
    @Test
    @DisplayName("Obtener reservas activas retorna 200 OK")
    public void testObtenerReservasActivas() throws Exception {
        when(reservaService.obtenerReservasActivas()).thenReturn(Arrays.asList(reservaMock));

        mockMvc.perform(get("/api/reservas/activas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    // ========================================================================
    // POST /api/reservas (CUBRE EL .map y .orElseGet)
    // ========================================================================
    @Test
    @DisplayName("Crear reserva con éxito retorna 201 CREATED")
    public void testCrearReservaExito() throws Exception {
        when(reservaService.realizarReserva(any(Reserva.class))).thenReturn(Optional.of(reservaMock));

        mockMvc.perform(post("/api/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reservaMock)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(100)));
    }

    @Test
    @DisplayName("Crear reserva con conflicto retorna 409 CONFLICT")
    public void testCrearReservaConflicto() throws Exception {
        when(reservaService.realizarReserva(any(Reserva.class))).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reservaMock)))
                .andExpect(status().isConflict());
    }

    // ========================================================================
    // DELETE /api/reservas/{id}
    // ========================================================================
    @Test
    @DisplayName("Cancelar reserva exitosamente retorna 200 OK")
    public void testCancelarReservaExito() throws Exception {
        when(authService.getEmpleadoByToken("token-valido")).thenReturn(usuarioMock);
        when(reservaService.cancelarReserva(100L, usuarioMock.getId())).thenReturn(true);

        mockMvc.perform(delete("/api/reservas/100")
                .header("Authorization", "token-valido"))
                .andExpect(status().isOk())
                .andExpect(content().string("Reserva cancelada con éxito"));
    }

    @Test
    @DisplayName("Cancelar reserva sin éxito retorna 403 FORBIDDEN")
    public void testCancelarReservaFallo() throws Exception {
        when(authService.getEmpleadoByToken("token-valido")).thenReturn(usuarioMock);
        when(reservaService.cancelarReserva(100L, usuarioMock.getId())).thenReturn(false);

        mockMvc.perform(delete("/api/reservas/100")
                .header("Authorization", "token-valido"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Cancelar reserva con token inválido retorna 401 UNAUTHORIZED")
    public void testCancelarReservaTokenInvalido() throws Exception {
        when(authService.getEmpleadoByToken("token-invalido")).thenReturn(null);

        mockMvc.perform(delete("/api/reservas/100")
                .header("Authorization", "token-invalido"))
                .andExpect(status().isUnauthorized());
    }

    // ========================================================================
    // PUT /api/reservas/{id}/extender
    // ========================================================================
    @Test
    @DisplayName("Extender reserva exitosamente retorna 200 OK")
    public void testExtenderReservaExito() throws Exception {
        when(authService.getEmpleadoByToken("token-valido")).thenReturn(usuarioMock);
        when(reservaService.extenderReserva(100L, 30, usuarioMock.getId())).thenReturn(Optional.of(reservaMock));

        mockMvc.perform(put("/api/reservas/100/extender")
                .param("minutos", "30")
                .header("Authorization", "token-valido"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(100)));
    }

    @Test
    @DisplayName("Extender reserva con sala ocupada retorna 409 CONFLICT")
    public void testExtenderReservaConflicto() throws Exception {
        when(authService.getEmpleadoByToken("token-valido")).thenReturn(usuarioMock);
        when(reservaService.extenderReserva(100L, 30, usuarioMock.getId())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/reservas/100/extender")
                .param("minutos", "30")
                .header("Authorization", "token-valido"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Extender reserva con token inválido retorna 401 UNAUTHORIZED")
    public void testExtenderReservaTokenInvalido() throws Exception {
        when(authService.getEmpleadoByToken("token-invalido")).thenReturn(null);

        mockMvc.perform(put("/api/reservas/100/extender")
                .param("minutos", "30")
                .header("Authorization", "token-invalido"))
                .andExpect(status().isUnauthorized());
    }

    // ========================================================================
    // PUT /api/reservas/{id}/devolver
    // ========================================================================
    @Test
    @DisplayName("Devolver sala exitosamente retorna 200 OK")
    public void testDevolverSalaExito() throws Exception {
        when(authService.getEmpleadoByToken("token-valido")).thenReturn(usuarioMock);
        when(reservaService.devolverSalaEarly(100L, usuarioMock.getId())).thenReturn(Optional.of(reservaMock));

        mockMvc.perform(put("/api/reservas/100/devolver")
                .header("Authorization", "token-valido"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(100)));
    }

    @Test
    @DisplayName("Devolver sala que no pertenece al usuario retorna 403 FORBIDDEN")
    public void testDevolverSalaFallo() throws Exception {
        when(authService.getEmpleadoByToken("token-valido")).thenReturn(usuarioMock);
        when(reservaService.devolverSalaEarly(100L, usuarioMock.getId())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/reservas/100/devolver")
                .header("Authorization", "token-valido"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Devolver sala con token inválido retorna 401 UNAUTHORIZED")
    public void testDevolverSalaTokenInvalido() throws Exception {
        when(authService.getEmpleadoByToken("token-invalido")).thenReturn(null);

        mockMvc.perform(put("/api/reservas/100/devolver")
                .header("Authorization", "token-invalido"))
                .andExpect(status().isUnauthorized());
    }
}