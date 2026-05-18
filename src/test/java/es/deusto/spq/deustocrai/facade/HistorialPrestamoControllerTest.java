package es.deusto.spq.deustocrai.facade;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import es.deusto.spq.deustocrai.entity.Prestamo;
import es.deusto.spq.deustocrai.entity.User;
import es.deusto.spq.deustocrai.service.AuthService;
import es.deusto.spq.deustocrai.service.PrestamoService;

// Usamos WebMvcTest solo para este controlador, es mucho más rápido y no levanta la base de datos
@WebMvcTest(PrestamoController.class)
public class HistorialPrestamoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Simulamos los servicios para no depender de la base de datos real
    @MockBean
    private AuthService authService;

    @MockBean
    private PrestamoService prestamoService;

    private String validToken = "token-simulado-valido";
    private User mockUser;

    @BeforeEach
    public void setUp() {
        // 1. Preparamos un usuario de mentira
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("estudiante@deusto.es");
        mockUser.setRole(User.Role.ESTUDIANTE);
        
        // 2. Le decimos al mock del AuthService qué debe responder
        when(authService.getEmpleadoByToken(validToken)).thenReturn(mockUser);
        when(authService.getEmpleadoByToken("token-inventado-invalido")).thenReturn(null);

        // 3. Le decimos al mock del PrestamoService qué debe devolver cuando pidamos el historial
        Prestamo p1 = new Prestamo();
        p1.setEstado(Prestamo.EstadoPrestamo.DEVUELTO);
        Prestamo p2 = new Prestamo();
        p2.setEstado(Prestamo.EstadoPrestamo.DEVUELTO);
        
        List<Prestamo> historialMock = Arrays.asList(p1, p2);
        when(prestamoService.obtenerHistorialPorUsuario(mockUser)).thenReturn(historialMock);
    }

    @Test
    @DisplayName("Debería retornar 200 OK y una lista JSON al pedir el historial con token válido")
    public void testHistorialDevuelveOk() throws Exception {
        mockMvc.perform(get("/api/prestamos/mi-historial")
                .header("Authorization", validToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Todos los préstamos del historial deben tener estado DEVUELTO")
    public void testHistorialSoloContieneDevueltos() throws Exception {
        mockMvc.perform(get("/api/prestamos/mi-historial")
                .header("Authorization", validToken))
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