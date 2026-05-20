package es.deusto.spq.deustocrai.facade;

import es.deusto.spq.deustocrai.entity.Prestamo;
import es.deusto.spq.deustocrai.entity.User;
import es.deusto.spq.deustocrai.service.AuthService;
import es.deusto.spq.deustocrai.service.PrestamoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;

@ExtendWith(MockitoExtension.class)
class PrestamoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PrestamoService prestamoService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private PrestamoController prestamoController;

    private User usuarioMock;
    private Prestamo prestamoMock;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(prestamoController).build();

        // Preparar usuario estudiante por defecto
        usuarioMock = new User();
        ReflectionTestUtils.setField(usuarioMock, "id", 1L);
        usuarioMock.setEmail("estudiante@opendeusto.es");
        usuarioMock.setRole(User.Role.ESTUDIANTE);

        // Preparar un préstamo mockeado
        prestamoMock = new Prestamo();
        ReflectionTestUtils.setField(prestamoMock, "id", 100L);
        prestamoMock.setEstado(Prestamo.EstadoPrestamo.ENTREGADO); // CORREGIDO: Estado válido
    }

    // ========================================================================
    // 1. TESTS: MIS PRESTAMOS
    // ========================================================================

    @Test
    void misPrestamos_ConTokenValido_RetornaLista() throws Exception {
        when(authService.getEmpleadoByToken("token-valido")).thenReturn(usuarioMock);
        when(prestamoService.obtenerPrestamosPorUsuario(usuarioMock)).thenReturn(Arrays.asList(prestamoMock));

        mockMvc.perform(get("/api/prestamos/mis-prestamos")
                .header("Authorization", "token-valido"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void misPrestamos_ConTokenInvalido_RetornaUnauthorized() throws Exception {
        when(authService.getEmpleadoByToken("token-invalido")).thenReturn(null);

        mockMvc.perform(get("/api/prestamos/mis-prestamos")
                .header("Authorization", "token-invalido"))
                .andExpect(status().isUnauthorized());
    }

    // ========================================================================
    // 2. TESTS: MI HISTORIAL
    // ========================================================================

    @Test
    void historialPrestamos_ConToken_RetornaHistorial() throws Exception {
        when(authService.getEmpleadoByToken("token-valido")).thenReturn(usuarioMock);
        when(prestamoService.obtenerHistorialPorUsuario(usuarioMock)).thenReturn(Arrays.asList(prestamoMock));

        mockMvc.perform(get("/api/prestamos/mi-historial")
                .header("Authorization", "token-valido"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void historialPrestamos_SinToken_RetornaUnauthorized() throws Exception {
        mockMvc.perform(get("/api/prestamos/mi-historial"))
                .andExpect(status().isUnauthorized());
    }

    // ========================================================================
    // 3. TESTS: PRESTAR LIBRO
    // ========================================================================

    @Test
    void prestarLibro_ConExito_RetornaOk() throws Exception {
        when(authService.getEmpleadoByToken("token-valido")).thenReturn(usuarioMock);
        when(prestamoService.realizarPrestamo(usuarioMock, 5L)).thenReturn(prestamoMock);

        mockMvc.perform(post("/api/prestamos/prestar/5")
                .header("Authorization", "token-valido"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void prestarLibro_NoDisponible_RetornaConflict() throws Exception {
        when(authService.getEmpleadoByToken("token-valido")).thenReturn(usuarioMock);
        when(prestamoService.realizarPrestamo(usuarioMock, 5L)).thenReturn(null);

        mockMvc.perform(post("/api/prestamos/prestar/5")
                .header("Authorization", "token-valido"))
                .andExpect(status().isConflict());
    }

    // ========================================================================
    // 4. TESTS: DEVOLVER RECURSO
    // ========================================================================

    @Test
    void devolverRecurso_ConExito_RetornaOk() throws Exception {
        when(authService.getEmpleadoByToken("token-valido")).thenReturn(usuarioMock);
        when(prestamoService.devolverPrestamo(usuarioMock, 100L)).thenReturn(true);

        mockMvc.perform(post("/api/prestamos/devolver/100")
                .header("Authorization", "token-valido"))
                .andExpect(status().isOk());
    }

    @Test
    void devolverRecurso_ErrorAlDevolver_RetornaBadRequest() throws Exception {
        when(authService.getEmpleadoByToken("token-valido")).thenReturn(usuarioMock);
        when(prestamoService.devolverPrestamo(usuarioMock, 100L)).thenReturn(false);

        mockMvc.perform(post("/api/prestamos/devolver/100")
                .header("Authorization", "token-valido"))
                .andExpect(status().isBadRequest());
    }

    // ========================================================================
    // 5. TESTS: TODOS LOS PRÉSTAMOS (BIBLIOTECARIO)
    // ========================================================================

    @Test
    void obtenerTodosLosPrestamos_SiendoBibliotecario_RetornaOk() throws Exception {
        usuarioMock.setRole(User.Role.BIBLIOTECARIO);
        when(authService.getEmpleadoByToken("token-biblio")).thenReturn(usuarioMock);
        when(prestamoService.obtenerTodosLosPrestamos()).thenReturn(Arrays.asList(prestamoMock));

        mockMvc.perform(get("/api/prestamos/todos")
                .header("Authorization", "token-biblio"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerTodosLosPrestamos_SiendoEstudiante_RetornaForbidden() throws Exception {
        when(authService.getEmpleadoByToken("token-estudiante")).thenReturn(usuarioMock); // Role = ESTUDIANTE

        mockMvc.perform(get("/api/prestamos/todos")
                .header("Authorization", "token-estudiante"))
                .andExpect(status().isForbidden());
    }

    // ========================================================================
    // 6. TESTS: CAMBIAR ESTADO PRÉSTAMO
    // ========================================================================

    @Test
    void cambiarEstadoPrestamo_SiendoBibliotecarioYExiste_RetornaOk() throws Exception {
        usuarioMock.setRole(User.Role.BIBLIOTECARIO);
        when(authService.getEmpleadoByToken("token-biblio")).thenReturn(usuarioMock);
        when(prestamoService.cambiarEstadoPrestamo(100L, Prestamo.EstadoPrestamo.DEVUELTO)).thenReturn(true);

        mockMvc.perform(put("/api/prestamos/100/estado")
                .header("Authorization", "token-biblio")
                .param("nuevoEstado", "DEVUELTO"))
                .andExpect(status().isOk());
    }

    @Test
    void cambiarEstadoPrestamo_NoEncontrado_RetornaNotFound() throws Exception {
        usuarioMock.setRole(User.Role.BIBLIOTECARIO);
        when(authService.getEmpleadoByToken("token-biblio")).thenReturn(usuarioMock);
        when(prestamoService.cambiarEstadoPrestamo(999L, Prestamo.EstadoPrestamo.DEVUELTO)).thenReturn(false);

        mockMvc.perform(put("/api/prestamos/999/estado")
                .header("Authorization", "token-biblio")
                .param("nuevoEstado", "DEVUELTO"))
                .andExpect(status().isNotFound());
    }

    // ========================================================================
    // 7. TESTS: PRESTAR MATERIAL
    // ========================================================================

    @Test
    void prestarMaterial_ConExito_RetornaOk() throws Exception {
        when(authService.getEmpleadoByToken("token-valido")).thenReturn(usuarioMock);
        when(prestamoService.realizarPrestamoMaterial(usuarioMock, 10L)).thenReturn(prestamoMock);

        mockMvc.perform(post("/api/prestamos/prestar-material/10")
                .header("Authorization", "token-valido"))
                .andExpect(status().isOk());
    }

    @Test
    void prestarMaterial_NoDisponible_RetornaConflict() throws Exception {
        when(authService.getEmpleadoByToken("token-valido")).thenReturn(usuarioMock);
        when(prestamoService.realizarPrestamoMaterial(usuarioMock, 10L)).thenReturn(null);

        mockMvc.perform(post("/api/prestamos/prestar-material/10")
                .header("Authorization", "token-valido"))
                .andExpect(status().isConflict());
    }

    // ========================================================================
    // 8. TESTS: LIBROS Y MATERIALES ACTIVOS (ROLES)
    // ========================================================================

    @Test
    void obtenerLibrosActivos_SiendoAdmin_RetornaOk() throws Exception {
        usuarioMock.setRole(User.Role.ADMIN);
        when(authService.getEmpleadoByToken("token-admin")).thenReturn(usuarioMock);
        when(prestamoService.obtenerPrestamosLibrosActivos()).thenReturn(Arrays.asList(prestamoMock));

        mockMvc.perform(get("/api/prestamos/libros-activos")
                .header("Authorization", "token-admin"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerMaterialesActivos_SiendoEstudiante_RetornaForbidden() throws Exception {
        when(authService.getEmpleadoByToken("token-estudiante")).thenReturn(usuarioMock); // ESTUDIANTE

        mockMvc.perform(get("/api/prestamos/materiales-activos")
                .header("Authorization", "token-estudiante"))
                .andExpect(status().isForbidden());
    }

    // ========================================================================
    // 9. TESTS: MIS ESTADÍSTICAS
    // ========================================================================

    @Test
    void misEstadisticas_ConExito_RetornaMap() throws Exception {
        when(authService.getEmpleadoByToken("token-valido")).thenReturn(usuarioMock);
        
        // CORREGIDO: Map<String, Integer> para coincidir con la firma del método
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", 5);
        when(prestamoService.obtenerEstadisticasUsuario(usuarioMock)).thenReturn(stats);

        mockMvc.perform(get("/api/prestamos/mis-estadisticas")
                .header("Authorization", "token-valido"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(5));
    }

    // ========================================================================
    // 10. TESTS: RENOVAR PRÉSTAMO
    // ========================================================================

    @Test
    void renovarPrestamo_ConExito_RetornaOk() throws Exception {
        when(authService.getEmpleadoByToken("token-valido")).thenReturn(usuarioMock);
        when(prestamoService.renovarPrestamo(100L, usuarioMock)).thenReturn(prestamoMock);

        mockMvc.perform(put("/api/prestamos/100/renovar")
                .header("Authorization", "token-valido"))
                .andExpect(status().isOk());
    }

    @Test
    void renovarPrestamo_LimiteAlcanzado_RetornaConflict() throws Exception {
        when(authService.getEmpleadoByToken("token-valido")).thenReturn(usuarioMock);
        when(prestamoService.renovarPrestamo(100L, usuarioMock))
                .thenThrow(new IllegalStateException("Límite de renovaciones"));

        mockMvc.perform(put("/api/prestamos/100/renovar")
                .header("Authorization", "token-valido"))
                .andExpect(status().isConflict());
    }

    @Test
    void renovarPrestamo_NoEncontrado_RetornaNotFound() throws Exception {
        when(authService.getEmpleadoByToken("token-valido")).thenReturn(usuarioMock);
        when(prestamoService.renovarPrestamo(999L, usuarioMock))
                .thenThrow(new IllegalArgumentException("No encontrado"));

        mockMvc.perform(put("/api/prestamos/999/renovar")
                .header("Authorization", "token-valido"))
                .andExpect(status().isNotFound());
    }
}