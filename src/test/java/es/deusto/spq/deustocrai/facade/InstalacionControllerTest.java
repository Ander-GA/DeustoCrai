package es.deusto.spq.deustocrai.facade;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;

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
}