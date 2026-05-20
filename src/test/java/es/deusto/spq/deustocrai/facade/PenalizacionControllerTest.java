package es.deusto.spq.deustocrai.facade;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import es.deusto.spq.deustocrai.dao.UserRepository;
import es.deusto.spq.deustocrai.entity.User;
import es.deusto.spq.deustocrai.service.AuthService;
import es.deusto.spq.deustocrai.service.PenalizacionService;

@Tag("Unitario")
@WebMvcTest(PenalizacionController.class)
public class PenalizacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private PenalizacionService penalizacionService;
    @MockitoBean private AuthService authService;
    @MockitoBean private UserRepository userRepository;

    private User admin;
    private User normalUser;

    @BeforeEach
    void setUp() {
        admin = new User();
        admin.setRole(User.Role.ADMIN);

        normalUser = new User();
        normalUser.setRole(User.Role.ESTUDIANTE);
    }

    @Test
    @DisplayName("Listar usuarios - Permiso Denegado")
    void testListarUsuariosForbidden() throws Exception {
        when(authService.getEmpleadoByToken("token-estudiante")).thenReturn(normalUser);

        mockMvc.perform(get("/api/penalizaciones/usuarios")
                .header("Authorization", "token-estudiante"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Aplicar penalización - Éxito Admin")
    void testAplicarPenalizacionSuccess() throws Exception {
        when(authService.getEmpleadoByToken("token-admin")).thenReturn(admin);
        User userModificado = new User();
        userModificado.setBloqueado(true);
        when(penalizacionService.aplicarPenalizacion(1L, 7)).thenReturn(userModificado);

        mockMvc.perform(put("/api/penalizaciones/1")
                .param("dias", "7")
                .header("Authorization", "token-admin"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Aplicar penalización - Usuario no encontrado lanza 404")
    void testAplicarPenalizacionNotFound() throws Exception {
        when(authService.getEmpleadoByToken("token-admin")).thenReturn(admin);
        when(penalizacionService.aplicarPenalizacion(99L, 7))
            .thenThrow(new IllegalArgumentException("Usuario no encontrado"));

        mockMvc.perform(put("/api/penalizaciones/99")
                .param("dias", "7")
                .header("Authorization", "token-admin"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Usuario no encontrado"));
    }

    @Test
    @DisplayName("Eliminar penalización - Éxito Admin")
    void testEliminarPenalizacionSuccess() throws Exception {
        when(authService.getEmpleadoByToken("token-admin")).thenReturn(admin);
        when(penalizacionService.eliminarPenalizacion(1L)).thenReturn(new User());

        mockMvc.perform(delete("/api/penalizaciones/1")
                .header("Authorization", "token-admin"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Listar penalizados - Éxito")
    void testListarPenalizadosSuccess() throws Exception {
        when(authService.getEmpleadoByToken("token-admin")).thenReturn(admin);
        when(penalizacionService.obtenerUsuariosPenalizados()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/penalizaciones")
                .header("Authorization", "token-admin"))
                .andExpect(status().isOk());
    }
}