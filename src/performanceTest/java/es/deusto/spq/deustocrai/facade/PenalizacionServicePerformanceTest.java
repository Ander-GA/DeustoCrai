package es.deusto.spq.deustocrai.facade;

// ─── UBICACIÓN: src/performanceTest/java/es/deusto/spq/deustocrai/facade/ ─────
// Mismo paquete que los demás performance tests del proyecto.
// Las clases de servicio se importan explícitamente desde el main source set.

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.StopWatch;

import es.deusto.spq.deustocrai.dao.UserRepository;
import es.deusto.spq.deustocrai.entity.Aviso;
import es.deusto.spq.deustocrai.entity.User;

import es.deusto.spq.deustocrai.service.AvisoService;
import es.deusto.spq.deustocrai.service.NotificacionService;
import es.deusto.spq.deustocrai.service.PenalizacionService;

@Tag("Rendimiento")
@ExtendWith(MockitoExtension.class)
public class PenalizacionServicePerformanceTest {

    @Mock private UserRepository       userRepository;
    @Mock private NotificacionService  notificacionService;
    @Mock private AvisoService         avisoService;

    @InjectMocks
    private PenalizacionService penalizacionService;

    private static final int  ITERACIONES_ALTA   = 1_000;
    private static final int  ITERACIONES_MEDIA  = 500;
    private static final int  ITERACIONES_CICLO  = 500;
    private static final long LIMITE_MS           = 3_000L;

    @BeforeEach
    void setUp() {
        lenient().when(userRepository.findById(anyLong())).thenAnswer(inv -> {
            Long id = inv.getArgument(0);
            User u = new User("Test", "User", "pass", "u" + id + "@deusto.es", User.Role.ESTUDIANTE);
            u.setId(id);
            return Optional.of(u);
        });
        lenient().when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(avisoService.crearAviso(any(), any(), any(), any())).thenReturn(null);
        lenient().doNothing().when(notificacionService).notificarPenalizacion(any(), anyInt());
    }

    // ─── aplicarPenalizacion ───────────────────────────────────────────────────

    @Test
    @DisplayName("Rendimiento: 1000 llamadas a aplicarPenalizacion < 3 s")
    void testRendimientoAplicarPenalizacion() throws Exception {
        StopWatch sw = new StopWatch();
        sw.start();
        for (int i = 1; i <= ITERACIONES_ALTA; i++) {
            penalizacionService.aplicarPenalizacion((long) i, 7);
        }
        sw.stop();
        imprimirResultado("aplicarPenalizacion", ITERACIONES_ALTA, sw.getTotalTimeMillis());
        assertTrue(sw.getTotalTimeMillis() < LIMITE_MS,
            "aplicarPenalizacion tardó demasiado: " + sw.getTotalTimeMillis() + " ms");
    }

    @Test
    @DisplayName("Rendimiento: 1000 llamadas a aplicarPenalizacion con distintas duraciones < 3 s")
    void testRendimientoAplicarVariasDuraciones() throws Exception {
        int[] dias = {1, 3, 7, 14, 30};
        StopWatch sw = new StopWatch();
        sw.start();
        for (int i = 1; i <= ITERACIONES_ALTA; i++) {
            penalizacionService.aplicarPenalizacion((long) i, dias[i % dias.length]);
        }
        sw.stop();
        imprimirResultado("aplicarPenalizacion (varias duraciones)", ITERACIONES_ALTA, sw.getTotalTimeMillis());
        assertTrue(sw.getTotalTimeMillis() < LIMITE_MS,
            "aplicarPenalizacion (varias duraciones) tardó demasiado: " + sw.getTotalTimeMillis() + " ms");
    }

    // ─── eliminarPenalizacion ──────────────────────────────────────────────────

    @Test
    @DisplayName("Rendimiento: 1000 llamadas a eliminarPenalizacion < 3 s")
    void testRendimientoEliminarPenalizacion() throws Exception {
        StopWatch sw = new StopWatch();
        sw.start();
        for (int i = 1; i <= ITERACIONES_ALTA; i++) {
            penalizacionService.eliminarPenalizacion((long) i);
        }
        sw.stop();
        imprimirResultado("eliminarPenalizacion", ITERACIONES_ALTA, sw.getTotalTimeMillis());
        assertTrue(sw.getTotalTimeMillis() < LIMITE_MS,
            "eliminarPenalizacion tardó demasiado: " + sw.getTotalTimeMillis() + " ms");
    }

    // ─── obtenerUsuariosPenalizados ────────────────────────────────────────────

    @Test
    @DisplayName("Rendimiento: 500 llamadas a obtenerUsuariosPenalizados con 200 usuarios < 3 s")
    void testRendimientoObtenerPenalizados_200Usuarios() throws Exception {
        when(userRepository.findAll()).thenReturn(generarUsuarios(200, 100));

        StopWatch sw = new StopWatch();
        sw.start();
        for (int i = 0; i < ITERACIONES_MEDIA; i++) {
            penalizacionService.obtenerUsuariosPenalizados();
        }
        sw.stop();
        imprimirResultado("obtenerUsuariosPenalizados (200 usuarios)", ITERACIONES_MEDIA, sw.getTotalTimeMillis());
        assertTrue(sw.getTotalTimeMillis() < LIMITE_MS,
            "obtenerUsuariosPenalizados (200) tardó demasiado: " + sw.getTotalTimeMillis() + " ms");
    }

    @Test
    @DisplayName("Rendimiento: 200 llamadas a obtenerUsuariosPenalizados con 1000 usuarios < 3 s")
    void testRendimientoObtenerPenalizados_1000Usuarios() throws Exception {
        when(userRepository.findAll()).thenReturn(generarUsuarios(1_000, 300));

        int iteraciones = 200;
        StopWatch sw = new StopWatch();
        sw.start();
        for (int i = 0; i < iteraciones; i++) {
            penalizacionService.obtenerUsuariosPenalizados();
        }
        sw.stop();
        imprimirResultado("obtenerUsuariosPenalizados (1000 usuarios)", iteraciones, sw.getTotalTimeMillis());
        assertTrue(sw.getTotalTimeMillis() < LIMITE_MS,
            "obtenerUsuariosPenalizados (1000) tardó demasiado: " + sw.getTotalTimeMillis() + " ms");
    }

    // ─── Ciclo mixto ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Rendimiento: 500 ciclos aplicar+eliminar penalización < 3 s")
    void testRendimientoCicloAplicarEliminar() throws Exception {
        StopWatch sw = new StopWatch();
        sw.start();
        for (int i = 1; i <= ITERACIONES_CICLO; i++) {
            penalizacionService.aplicarPenalizacion((long) i, 7);
            penalizacionService.eliminarPenalizacion((long) i);
        }
        sw.stop();
        imprimirResultado("ciclo aplicar+eliminar", ITERACIONES_CICLO, sw.getTotalTimeMillis());
        assertTrue(sw.getTotalTimeMillis() < LIMITE_MS,
            "Ciclo aplicar+eliminar tardó demasiado: " + sw.getTotalTimeMillis() + " ms");
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private List<User> generarUsuarios(int total, int bloqueados) {
        List<User> lista = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            User u = new User("N" + i, "A" + i, "p", "u" + i + "@deusto.es", User.Role.ESTUDIANTE);
            u.setId((long) i);
            u.setBloqueado(i < bloqueados);
            lista.add(u);
        }
        return lista;
    }

    private void imprimirResultado(String nombre, int iteraciones, long totalMs) {
        System.out.println("====== RENDIMIENTO: " + nombre + " ======");
        System.out.println("Iteraciones : " + iteraciones);
        System.out.println("Tiempo total: " + totalMs + " ms");
        System.out.printf("Media/llam. : %.3f ms%n", totalMs / (double) iteraciones);
        System.out.println("============================================");
    }
}