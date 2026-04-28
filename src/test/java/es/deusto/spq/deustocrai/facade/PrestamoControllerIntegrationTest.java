package es.deusto.spq.deustocrai.facade;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import es.deusto.spq.deustocrai.entity.Prestamo;

@SpringBootTest
@AutoConfigureMockMvc
public class PrestamoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Debería denegar el acceso (403 Forbidden) a un usuario con un token INVENTADO al cambiar estado")
    public void testCambiarEstadoConTokenInvalido() throws Exception {
        // Al enviar un token falso, Spring deja pasar la petición al controlador,
        // pero tu authService.getEmpleadoByToken("token-falso") devolverá null,
        // por lo que saltará tu validación y devolverá el 403 FORBIDDEN.
        mockMvc.perform(put("/api/prestamos/1/estado")
                .header("Authorization", "token-falso-inventado")
                .param("nuevoEstado", Prestamo.EstadoPrestamo.ENTREGADO.toString()))
                .andExpect(status().isForbidden()); 
    }

    @Test
    @DisplayName("Debería devolver 400 Bad Request si no se envía el header Authorization")
    public void testCambiarEstadoSinHeader() throws Exception {
        // Si no enviamos el header en absoluto, Spring aborta la petición con un 400
        // porque el @RequestHeader es obligatorio por defecto.
        mockMvc.perform(put("/api/prestamos/1/estado")
                .param("nuevoEstado", Prestamo.EstadoPrestamo.ENTREGADO.toString()))
                .andExpect(status().isBadRequest()); 
    }
}