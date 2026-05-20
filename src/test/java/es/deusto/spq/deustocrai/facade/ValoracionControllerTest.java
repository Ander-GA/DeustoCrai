package es.deusto.spq.deustocrai.facade;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import es.deusto.spq.deustocrai.entity.User;
import es.deusto.spq.deustocrai.entity.Valoracion;
import es.deusto.spq.deustocrai.service.AuthService;
import es.deusto.spq.deustocrai.service.ValoracionService;

@Tag("Unitario")
@WebMvcTest(ValoracionController.class)
public class ValoracionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ValoracionService valoracionService;

    @MockitoBean
    private AuthService authService;

    private User usuarioMock;
    private Valoracion valoracionMock;

    @BeforeEach
    void setUp() {
        usuarioMock = new User();
        ReflectionTestUtils.setField(usuarioMock, "id", 1L);
        usuarioMock.setEmail("estudiante@opendeusto.es");
        usuarioMock.setRole(User.Role.ESTUDIANTE);

        valoracionMock = new Valoracion();
        ReflectionTestUtils.setField(valoracionMock, "id", 100L);
        ReflectionTestUtils.setField(valoracionMock, "puntuacion", 5);
        ReflectionTestUtils.setField(valoracionMock, "comentario", "Excelente recurso");
    }

    // ========================================================================
    // GET /api/valoraciones/recurso/{recursoId}
    // ========================================================================
    @Test
    @DisplayName("Obtener reseñas retorna 200 OK y la lista de valoraciones")
    public void testObtenerResenas() throws Exception {
        when(valoracionService.obtenerResenasDeRecurso(10L)).thenReturn(Arrays.asList(valoracionMock));

        mockMvc.perform(get("/api/valoraciones/recurso/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].puntuacion", is(5)))
                .andExpect(jsonPath("$[0].comentario", is("Excelente recurso")));
    }

    // ========================================================================
    // POST /api/valoraciones/recurso/{recursoId}
    // ========================================================================
    @Test
    @DisplayName("Publicar reseña exitosamente retorna 201 CREATED")
    public void testPublicarResenaExito() throws Exception {
        when(authService.getEmpleadoByToken("token-valido")).thenReturn(usuarioMock);
        when(valoracionService.dejarResena(eq(usuarioMock), eq(10L), eq(5), eq("Muy bueno")))
                .thenReturn(valoracionMock);

        mockMvc.perform(post("/api/valoraciones/recurso/10")
                .header("Authorization", "token-valido")
                .param("puntuacion", "5")
                .param("comentario", "Muy bueno"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.puntuacion", is(5)));
    }

    @Test
    @DisplayName("Publicar reseña sin token retorna 401 UNAUTHORIZED")
    public void testPublicarResenaSinToken() throws Exception {
        mockMvc.perform(post("/api/valoraciones/recurso/10")
                .param("puntuacion", "5"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"error\": \"No se ha recibido el token.\"}"));
    }

    @Test
    @DisplayName("Publicar reseña con token inválido retorna 401 UNAUTHORIZED")
    public void testPublicarResenaTokenInvalido() throws Exception {
        when(authService.getEmpleadoByToken("token-invalido")).thenReturn(null);

        mockMvc.perform(post("/api/valoraciones/recurso/10")
                .header("Authorization", "token-invalido")
                .param("puntuacion", "5"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"error\": \"Token inválido.\"}"));
    }

    @Test
    @DisplayName("Publicar reseña sin permisos (ej. no devuelto) retorna 403 FORBIDDEN")
    public void testPublicarResenaNoDevuelto() throws Exception {
        when(authService.getEmpleadoByToken("token-valido")).thenReturn(usuarioMock);
        // Simulamos la excepción que lanza el servicio si no se cumplen las reglas de negocio
        when(valoracionService.dejarResena(any(User.class), anyLong(), anyInt(), any()))
                .thenThrow(new IllegalStateException("Debes devolver el material primero."));

        mockMvc.perform(post("/api/valoraciones/recurso/10")
                .header("Authorization", "token-valido")
                .param("puntuacion", "5")
                .param("comentario", "Buen material"))
                .andExpect(status().isForbidden())
                .andExpect(content().json("{\"error\": \"Debes devolver el material primero.\"}"));
    }

    @Test
    @DisplayName("Publicar reseña con datos erróneos retorna 400 BAD REQUEST")
    public void testPublicarResenaDatosErrones() throws Exception {
        when(authService.getEmpleadoByToken("token-valido")).thenReturn(usuarioMock);
        // Simulamos la excepción para cuando la puntuación no es válida (ej. > 5)
        when(valoracionService.dejarResena(any(User.class), anyLong(), anyInt(), any()))
                .thenThrow(new IllegalArgumentException("Puntuación inválida."));

        mockMvc.perform(post("/api/valoraciones/recurso/10")
                .header("Authorization", "token-valido")
                .param("puntuacion", "9")
                .param("comentario", "Invalido"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"error\": \"Puntuación inválida.\"}"));
    }
}