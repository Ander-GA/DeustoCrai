package es.deusto.spq.deustocrai.facade;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Tag;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.deusto.spq.deustocrai.dao.InstalacionRepository;
import es.deusto.spq.deustocrai.dao.ReservaInstalacionRepository;
import es.deusto.spq.deustocrai.entity.ReservaInstalacion;
import es.deusto.spq.deustocrai.entity.User;
import es.deusto.spq.deustocrai.service.AuthService;
import es.deusto.spq.deustocrai.service.InstalacionService;

@Tag("Unitario")
@WebMvcTest(InstalacionController.class)
public class InstalacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean private InstalacionService instalacionService;
    @MockitoBean private InstalacionRepository instalacionRepo;
    @MockitoBean private ReservaInstalacionRepository reservaRepo;
    @MockitoBean private AuthService authService;

    private User estudiante;
    private User bibliotecario;
    private ReservaInstalacion reservaMock;

    @BeforeEach
    void setUp() {
        estudiante = new User();
        estudiante.setId(1L);
        estudiante.setRole(User.Role.ESTUDIANTE);

        bibliotecario = new User();
        bibliotecario.setId(2L);
        bibliotecario.setRole(User.Role.BIBLIOTECARIO);

        reservaMock = new ReservaInstalacion();
        org.springframework.test.util.ReflectionTestUtils.setField(reservaMock, "id", 100L);
    }

    // --- TESTS GET /instalaciones ---
    @Test
    @DisplayName("GET /instalaciones - Retorna la lista de instalaciones")
    public void testGetInstalaciones() throws Exception {
        when(instalacionRepo.findAll()).thenReturn(Arrays.asList());
        mockMvc.perform(get("/api/deportes/instalaciones"))
                .andExpect(status().isOk());
    }

    // --- TESTS POST /solicitar ---
    @Test
    @DisplayName("POST /solicitar - Retorna 200 OK si la solicitud se envía")
    public void testSolicitarReservaExito() throws Exception {
        when(authService.getEmpleadoByToken("token-valido")).thenReturn(estudiante);
        when(instalacionService.solicitarReserva(any(ReservaInstalacion.class))).thenReturn("OK");

        mockMvc.perform(post("/api/deportes/solicitar")
                .header("Authorization", "token-valido")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reservaMock)))
                .andExpect(status().isOk())
                .andExpect(content().string("Solicitud enviada. A la espera de aprobación."));
    }

    @Test
    @DisplayName("POST /solicitar - Retorna 400 si hay error al solicitar (ej. tope alcanzado)")
    public void testSolicitarReservaFallo() throws Exception {
        when(authService.getEmpleadoByToken("token-valido")).thenReturn(estudiante);
        when(instalacionService.solicitarReserva(any(ReservaInstalacion.class))).thenReturn("Límite de reservas alcanzado");

        mockMvc.perform(post("/api/deportes/solicitar")
                .header("Authorization", "token-valido")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reservaMock)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Límite de reservas alcanzado"));
    }

    @Test
    @DisplayName("POST /solicitar - Retorna 401 si no hay usuario válido")
    public void testSolicitarReservaNoAutorizado() throws Exception {
        when(authService.getEmpleadoByToken("token-invalido")).thenReturn(null);

        mockMvc.perform(post("/api/deportes/solicitar")
                .header("Authorization", "token-invalido")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reservaMock)))
                .andExpect(status().isUnauthorized());
    }

    // --- TESTS GET /pendientes ---
    @Test
    @DisplayName("GET /pendientes - Retorna 200 OK si es Bibliotecario")
    public void testGetPendientesExito() throws Exception {
        when(authService.getEmpleadoByToken("token-biblio")).thenReturn(bibliotecario);
        when(reservaRepo.findByEstado(ReservaInstalacion.EstadoReserva.PENDIENTE)).thenReturn(Arrays.asList(reservaMock));

        mockMvc.perform(get("/api/deportes/pendientes")
                .header("Authorization", "token-biblio"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /pendientes - Retorna 403 Forbidden si un Estudiante intenta verlo")
    public void testGetPendientesProhibido() throws Exception {
        when(authService.getEmpleadoByToken("token-estudiante")).thenReturn(estudiante);

        mockMvc.perform(get("/api/deportes/pendientes")
                .header("Authorization", "token-estudiante"))
                .andExpect(status().isForbidden());
    }

    // --- TESTS PUT /procesar/{id} ---
    @Test
    @DisplayName("PUT /procesar/{id} - Retorna 200 OK al aprobar/rechazar reserva")
    public void testProcesarReservaExito() throws Exception {
        when(authService.getEmpleadoByToken("token-biblio")).thenReturn(bibliotecario);
        when(instalacionService.procesarSolicitud(eq(100L), any(ReservaInstalacion.EstadoReserva.class))).thenReturn(true);

        mockMvc.perform(put("/api/deportes/procesar/100")
                .header("Authorization", "token-biblio")
                .param("estado", "APROBADA"))
                .andExpect(status().isOk())
                .andExpect(content().string("Reserva actualizada a: APROBADA"));
    }

    @Test
    @DisplayName("PUT /procesar/{id} - Retorna 403 Forbidden si un Estudiante intenta procesar")
    public void testProcesarReservaProhibido() throws Exception {
        when(authService.getEmpleadoByToken("token-estudiante")).thenReturn(estudiante);

        mockMvc.perform(put("/api/deportes/procesar/100")
                .header("Authorization", "token-estudiante")
                .param("estado", "APROBADA"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /procesar/{id} - Retorna 404 Not Found si la solicitud no existe")
    public void testProcesarReservaNoEncontrada() throws Exception {
        when(authService.getEmpleadoByToken("token-biblio")).thenReturn(bibliotecario);
        when(instalacionService.procesarSolicitud(eq(999L), any(ReservaInstalacion.EstadoReserva.class))).thenReturn(false);

        mockMvc.perform(put("/api/deportes/procesar/999")
                .header("Authorization", "token-biblio")
                .param("estado", "RECHAZADA"))
                .andExpect(status().isNotFound());
    }

    // --- TESTS GET /eventos ---
    @Test
    @DisplayName("GET /eventos - Retorna la lista de eventos del calendario")
    public void testObtenerEventosCalendario() throws Exception {
        Map<String, Object> evento = new HashMap<>();
        evento.put("title", "Partido Padel");
        when(instalacionService.obtenerEventosCalendario()).thenReturn(Arrays.asList(evento));

        mockMvc.perform(get("/api/deportes/eventos"))
                .andExpect(status().isOk());
    }

    // --- TESTS GET /mis-reservas ---
    @Test
    @DisplayName("GET /mis-reservas - Retorna 200 OK con las reservas del usuario")
    public void testGetMisReservasExito() throws Exception {
        when(authService.getEmpleadoByToken("token-valido")).thenReturn(estudiante);
        when(reservaRepo.findByUsuario(estudiante)).thenReturn(Arrays.asList(reservaMock));

        mockMvc.perform(get("/api/deportes/mis-reservas")
                .header("Authorization", "token-valido"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /mis-reservas - Retorna 401 Unauthorized si el token no es válido")
    public void testGetMisReservasNoAutorizado() throws Exception {
        when(authService.getEmpleadoByToken("token-invalido")).thenReturn(null);

        mockMvc.perform(get("/api/deportes/mis-reservas")
                .header("Authorization", "token-invalido"))
                .andExpect(status().isUnauthorized());
    }

    // --- TESTS DELETE /mis-reservas/{id} (CANCELAR) ---
    @Test
    @DisplayName("DELETE /mis-reservas/{id} - Retorna 200 OK al cancelar reserva")
    public void testCancelarMiReservaExito() throws Exception {
        when(authService.getEmpleadoByToken("token-valido")).thenReturn(estudiante);
        when(instalacionService.cancelarReservaUsuario(100L, estudiante.getId())).thenReturn(true);

        mockMvc.perform(delete("/api/deportes/mis-reservas/100")
                .header("Authorization", "token-valido"))
                .andExpect(status().isOk())
                .andExpect(content().string("Reserva cancelada correctamente."));
    }

    @Test
    @DisplayName("DELETE /mis-reservas/{id} - Retorna 403 Forbidden si no se pudo cancelar")
    public void testCancelarMiReservaFallo() throws Exception {
        when(authService.getEmpleadoByToken("token-valido")).thenReturn(estudiante);
        when(instalacionService.cancelarReservaUsuario(100L, estudiante.getId())).thenReturn(false);

        mockMvc.perform(delete("/api/deportes/mis-reservas/100")
                .header("Authorization", "token-valido"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("No se pudo cancelar la reserva."));
    }

    @Test
    @DisplayName("DELETE /mis-reservas/{id} - Retorna 401 si el usuario no es válido")
    public void testCancelarMiReservaNoAutorizado() throws Exception {
        when(authService.getEmpleadoByToken("token-invalido")).thenReturn(null);

        mockMvc.perform(delete("/api/deportes/mis-reservas/100")
                .header("Authorization", "token-invalido"))
                .andExpect(status().isUnauthorized());
    }

    // --- TESTS PUT /mis-reservas/{id} (MODIFICAR) ---
    @Test
    @DisplayName("PUT /mis-reservas/{id} - Retorna 200 OK al modificar reserva")
    public void testModificarMiReservaExito() throws Exception {
        when(authService.getEmpleadoByToken("token-valido")).thenReturn(estudiante);
        when(instalacionService.modificarReservaUsuario(eq(100L), eq(estudiante.getId()), any(), any())).thenReturn("OK");

        mockMvc.perform(put("/api/deportes/mis-reservas/100")
                .header("Authorization", "token-valido")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reservaMock)))
                .andExpect(status().isOk())
                .andExpect(content().string("Reserva modificada correctamente."));
    }

    @Test
    @DisplayName("PUT /mis-reservas/{id} - Retorna 400 Bad Request si la modificación falla")
    public void testModificarMiReservaFallo() throws Exception {
        when(authService.getEmpleadoByToken("token-valido")).thenReturn(estudiante);
        // Simulamos que el horario ya está ocupado
        when(instalacionService.modificarReservaUsuario(eq(100L), eq(estudiante.getId()), any(), any())).thenReturn("Horario no disponible");

        mockMvc.perform(put("/api/deportes/mis-reservas/100")
                .header("Authorization", "token-valido")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reservaMock)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Horario no disponible"));
    }

    @Test
    @DisplayName("PUT /mis-reservas/{id} - Retorna 401 si no hay usuario válido")
    public void testModificarMiReservaNoAutorizado() throws Exception {
        when(authService.getEmpleadoByToken("token-invalido")).thenReturn(null);

        mockMvc.perform(put("/api/deportes/mis-reservas/100")
                .header("Authorization", "token-invalido")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reservaMock)))
                .andExpect(status().isUnauthorized());
    }
}