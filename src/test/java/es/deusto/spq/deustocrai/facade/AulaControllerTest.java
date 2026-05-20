package es.deusto.spq.deustocrai.facade;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Tag;

import es.deusto.spq.deustocrai.dao.AulaRepository;
import es.deusto.spq.deustocrai.dao.BloqueoSalaRepository;
import es.deusto.spq.deustocrai.dao.ReservaRepository;
import es.deusto.spq.deustocrai.dao.UserRepository;
import es.deusto.spq.deustocrai.entity.Aula;
import es.deusto.spq.deustocrai.entity.Reserva;
import es.deusto.spq.deustocrai.entity.User;
import es.deusto.spq.deustocrai.service.AuthService;
import es.deusto.spq.deustocrai.service.ReservaService;

@Tag("Unitario")
@WebMvcTest(AulaController.class)
public class AulaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Simulamos TODAS las dependencias que el AulaController inyecta
    @MockitoBean private BloqueoSalaRepository bloqueoSalaRepository;
    @MockitoBean private ReservaService reservaService;
    @MockitoBean private AulaRepository aulaRepository;
    @MockitoBean private ReservaRepository reservaRepository;
    @MockitoBean private AuthService authService;
    @MockitoBean private UserRepository userRepository;

    private User estudiante;
    private User admin;
    private Aula aula;
    private Reserva reservaMock;
    
    // JSON puro para evitar bucles infinitos de Jackson y errores de fecha (400)
    private final String jsonReserva = "{\"usuario\": {\"id\": 1}, \"aula\": {\"id\": 10}}";
    private final String jsonBloqueo = "{\"motivo\": \"Obras\", \"fechaInicio\": \"2026-05-25T10:00:00\", \"fechaFin\": \"2026-05-26T10:00:00\"}";

    @BeforeEach
    void setUp() {
        estudiante = new User();
        estudiante.setId(1L);
        estudiante.setRole(User.Role.ESTUDIANTE);
        estudiante.setBloqueado(false);

        admin = new User();
        admin.setId(2L);
        admin.setRole(User.Role.ADMIN);

        aula = new Aula();
        // Inyectamos el ID a la fuerza para el test:
        org.springframework.test.util.ReflectionTestUtils.setField(aula, "id", 10L);

        reservaMock = new Reserva();
        reservaMock.setUsuario(estudiante);
        reservaMock.setAula(aula);
    }

    // --- TESTS listarSalas ---
    @Test
    @DisplayName("GET /api/salas - Retorna lista de aulas")
    public void testListarSalas() throws Exception {
        when(aulaRepository.findAll()).thenReturn(Arrays.asList(aula));
        mockMvc.perform(get("/api/salas"))
                .andExpect(status().isOk());
    }

    // --- TESTS reservarSala (Lógica de penalizaciones) ---
    @Test
    @DisplayName("POST /reservar - Retorna 401 si el usuario no existe")
    public void testReservarSalaUserNoExiste() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/salas/reservar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonReserva))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /reservar - Retorna 403 si el usuario sigue penalizado")
    public void testReservarSalaPenalizado() throws Exception {
        estudiante.setBloqueado(true);
        // Castigado hasta mañana
        estudiante.setFechaFinPenalizacion(LocalDateTime.now().plusDays(1)); 
        when(userRepository.findById(1L)).thenReturn(Optional.of(estudiante));

        mockMvc.perform(post("/api/salas/reservar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonReserva))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /reservar - Levanta castigo si el tiempo pasó y reserva con éxito")
    public void testReservarSalaPenalizacionExpirada() throws Exception {
        estudiante.setBloqueado(true);
        // El castigo terminó ayer
        estudiante.setFechaFinPenalizacion(LocalDateTime.now().minusDays(1)); 
        when(userRepository.findById(1L)).thenReturn(Optional.of(estudiante));
        when(reservaService.realizarReserva(any())).thenReturn(Optional.of(reservaMock));

        mockMvc.perform(post("/api/salas/reservar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonReserva))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /reservar - Retorna 409 si hay conflicto de horario")
    public void testReservarSalaConflicto() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(estudiante));
        when(reservaService.realizarReserva(any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/salas/reservar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonReserva))
                .andExpect(status().isConflict());
    }

    // --- TESTS mis-reservas ---
    @Test
    @DisplayName("GET /mis-reservas - Retorna reservas del usuario logueado")
    public void testMisReservasSuccess() throws Exception {
        when(authService.getEmpleadoByToken("token")).thenReturn(estudiante);
        when(reservaRepository.findAll()).thenReturn(Arrays.asList(reservaMock));

        mockMvc.perform(get("/api/salas/mis-reservas")
                .header("Authorization", "token"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /mis-reservas - Retorna 401 si token no es válido")
    public void testMisReservasFail() throws Exception {
        when(authService.getEmpleadoByToken("token")).thenReturn(null);

        mockMvc.perform(get("/api/salas/mis-reservas")
                .header("Authorization", "token"))
                .andExpect(status().isUnauthorized());
    }

    // --- TESTS bloquearSala ---
    @Test
    @DisplayName("POST /bloquear - Éxito si es Admin/Bibliotecario")
    public void testBloquearSalaSuccess() throws Exception {
        when(authService.getEmpleadoByToken("token-admin")).thenReturn(admin);
        when(aulaRepository.findById(10L)).thenReturn(Optional.of(aula));

        mockMvc.perform(post("/api/salas/10/bloquear")
                .header("Authorization", "token-admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBloqueo))
                .andExpect(status().isOk())
                .andExpect(content().string("Sala bloqueada correctamente por motivo: Obras"));
    }

    @Test
    @DisplayName("POST /bloquear - Falla si no es Admin/Bibliotecario")
    public void testBloquearSalaForbbiden() throws Exception {
        // Estudiante intentando bloquear
        when(authService.getEmpleadoByToken("token-estudiante")).thenReturn(estudiante); 

        mockMvc.perform(post("/api/salas/10/bloquear")
                .header("Authorization", "token-estudiante")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBloqueo))
                .andExpect(status().isForbidden());
    }

    // --- TESTS listarReservasUsuario y consultarReservasAula ---
    @Test
    @DisplayName("GET /usuario/{usuarioId} - Retorna las reservas filtradas por ID de usuario")
    public void testListarReservasUsuario() throws Exception {
        Reserva reservaOtro = new Reserva();
        User otroUser = new User();
        otroUser.setId(99L);
        reservaOtro.setUsuario(otroUser);

        when(reservaRepository.findAll()).thenReturn(Arrays.asList(reservaMock, reservaOtro));

        mockMvc.perform(get("/api/salas/usuario/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /{id}/reservas - Retorna las reservas de un aula concreta")
    public void testConsultarReservasAula() throws Exception {
        when(reservaRepository.findByAulaId(10L)).thenReturn(Arrays.asList(reservaMock));

        mockMvc.perform(get("/api/salas/10/reservas"))
                .andExpect(status().isOk());
    }
}