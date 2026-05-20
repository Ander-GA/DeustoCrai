package es.deusto.spq.deustocrai.facade;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.StopWatch;

import es.deusto.spq.deustocrai.dao.AvisoRepository;
import es.deusto.spq.deustocrai.dao.LibroRepository;
import es.deusto.spq.deustocrai.dao.MaterialRepository;
import es.deusto.spq.deustocrai.dao.PrestamoRepository;
import es.deusto.spq.deustocrai.dao.ReservaRepository;
import es.deusto.spq.deustocrai.dao.UserRepository;
import es.deusto.spq.deustocrai.entity.Aviso;
import es.deusto.spq.deustocrai.entity.Prestamo;
import es.deusto.spq.deustocrai.entity.User;

// ── Importación explícita del servicio (paquete diferente al test) ─────────────
import es.deusto.spq.deustocrai.service.EstadisticasService;

@Tag("Rendimiento")
@ExtendWith(MockitoExtension.class)
public class EstadisticasServicePerformanceTest {

    @Mock private PrestamoRepository prestamoRepository;
    @Mock private UserRepository     userRepository;
    @Mock private LibroRepository    libroRepository;
    @Mock private MaterialRepository materialRepository;
    @Mock private ReservaRepository  reservaRepository;
    @Mock private AvisoRepository    avisoRepository;

    @InjectMocks
    private EstadisticasService estadisticasService;

    private static final long LIMITE_MS = 3_000L;

    @BeforeEach
    void stubContadores() {
        when(prestamoRepository.count()).thenReturn(0L);
        when(userRepository.count()).thenReturn(0L);
        when(libroRepository.count()).thenReturn(0L);
        when(materialRepository.count()).thenReturn(0L);
        when(reservaRepository.count()).thenReturn(0L);
        when(avisoRepository.count()).thenReturn(0L);
    }

    // ─── BD vacía ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Rendimiento: 1000 llamadas a obtenerResumen con BD vacía < 3 s")
    void testRendimientoResumenBdVacia() throws Exception {
        when(prestamoRepository.findAll()).thenReturn(Collections.emptyList());
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        when(avisoRepository.findAll()).thenReturn(Collections.emptyList());

        int iteraciones = 1_000;
        StopWatch sw = new StopWatch();
        sw.start();
        for (int i = 0; i < iteraciones; i++) estadisticasService.obtenerResumen();
        sw.stop();

        imprimirResultado("obtenerResumen (BD vacía)", iteraciones, sw.getTotalTimeMillis());
        assertTrue(sw.getTotalTimeMillis() < LIMITE_MS,
            "obtenerResumen (vacía) tardó demasiado: " + sw.getTotalTimeMillis() + " ms");
    }

    // ─── Volumen medio ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Rendimiento: 500 llamadas con 200 préstamos, 50 usuarios y 30 avisos < 3 s")
    void testRendimientoResumen_volumenMedio() throws Exception {
        when(prestamoRepository.findAll()).thenReturn(generarPrestamos(200, 80));
        when(userRepository.findAll()).thenReturn(generarUsuarios(50, 10));
        when(avisoRepository.findAll()).thenReturn(generarAvisos(30, 12));

        int iteraciones = 500;
        StopWatch sw = new StopWatch();
        sw.start();
        for (int i = 0; i < iteraciones; i++) estadisticasService.obtenerResumen();
        sw.stop();

        imprimirResultado("obtenerResumen (volumen medio)", iteraciones, sw.getTotalTimeMillis());
        assertTrue(sw.getTotalTimeMillis() < LIMITE_MS,
            "obtenerResumen (volumen medio) tardó demasiado: " + sw.getTotalTimeMillis() + " ms");
    }

    // ─── Volumen alto ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Rendimiento: 200 llamadas con 1000 préstamos < 3 s")
    void testRendimientoResumen_1000Prestamos() throws Exception {
        when(prestamoRepository.findAll()).thenReturn(generarPrestamos(1_000, 400));
        when(userRepository.findAll()).thenReturn(generarUsuarios(200, 50));
        when(avisoRepository.findAll()).thenReturn(generarAvisos(150, 60));

        int iteraciones = 200;
        StopWatch sw = new StopWatch();
        sw.start();
        for (int i = 0; i < iteraciones; i++) estadisticasService.obtenerResumen();
        sw.stop();

        imprimirResultado("obtenerResumen (1000 préstamos)", iteraciones, sw.getTotalTimeMillis());
        assertTrue(sw.getTotalTimeMillis() < LIMITE_MS,
            "obtenerResumen (1000 préstamos) tardó demasiado: " + sw.getTotalTimeMillis() + " ms");
    }

    // ─── Mapa de recursos con muchos títulos distintos ────────────────────────

    @Test
    @DisplayName("Rendimiento: 500 llamadas con 500 recursos distintos (mapa grande) < 3 s")
    void testRendimientoResumen_500RecursosDistintos() throws Exception {
        List<Prestamo> prestamos = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            Prestamo p = new Prestamo();
            p.setNombreRecursoHistorico("Recurso-" + i);
            p.setEstado(Prestamo.EstadoPrestamo.PENDIENTE_ENTREGA);
            prestamos.add(p);
        }
        when(prestamoRepository.findAll()).thenReturn(prestamos);
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        when(avisoRepository.findAll()).thenReturn(Collections.emptyList());

        int iteraciones = 500;
        StopWatch sw = new StopWatch();
        sw.start();
        for (int i = 0; i < iteraciones; i++) estadisticasService.obtenerResumen();
        sw.stop();

        imprimirResultado("obtenerResumen (500 recursos distintos)", iteraciones, sw.getTotalTimeMillis());
        assertTrue(sw.getTotalTimeMillis() < LIMITE_MS,
            "500 recursos distintos tardó demasiado: " + sw.getTotalTimeMillis() + " ms");
    }

    // ─── Agrupación intensiva (mismo recurso) ─────────────────────────────────

    @Test
    @DisplayName("Rendimiento: 500 llamadas con 1000 préstamos del mismo recurso < 3 s")
    void testRendimientoResumen_mismoRecurso() throws Exception {
        List<Prestamo> prestamos = new ArrayList<>();
        for (int i = 0; i < 1_000; i++) {
            Prestamo p = new Prestamo();
            p.setNombreRecursoHistorico("El Quijote");
            boolean devuelto = (i % 3 == 0);
            p.setEstado(devuelto ? Prestamo.EstadoPrestamo.DEVUELTO : Prestamo.EstadoPrestamo.PENDIENTE_ENTREGA);
            if (devuelto) p.setFechaDevolucionReal(LocalDate.now().minusDays(1));
            prestamos.add(p);
        }
        when(prestamoRepository.findAll()).thenReturn(prestamos);
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        when(avisoRepository.findAll()).thenReturn(Collections.emptyList());

        int iteraciones = 500;
        StopWatch sw = new StopWatch();
        sw.start();
        for (int i = 0; i < iteraciones; i++) estadisticasService.obtenerResumen();
        sw.stop();

        imprimirResultado("obtenerResumen (1000 x mismo recurso)", iteraciones, sw.getTotalTimeMillis());
        assertTrue(sw.getTotalTimeMillis() < LIMITE_MS,
            "Agrupación intensiva tardó demasiado: " + sw.getTotalTimeMillis() + " ms");
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private List<Prestamo> generarPrestamos(int total, int activos) {
        List<Prestamo> lista = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            Prestamo p = new Prestamo();
            p.setNombreRecursoHistorico("Recurso-" + (i % 20));
            boolean devuelto = (i >= activos);
            p.setEstado(devuelto ? Prestamo.EstadoPrestamo.DEVUELTO : Prestamo.EstadoPrestamo.PENDIENTE_ENTREGA);
            if (devuelto) p.setFechaDevolucionReal(LocalDate.now().minusDays(1));
            lista.add(p);
        }
        return lista;
    }

    private List<User> generarUsuarios(int total, int bloqueados) {
        List<User> lista = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            User u = new User("N" + i, "A" + i, "p", "u" + i + "@deusto.es", User.Role.ESTUDIANTE);
            u.setBloqueado(i < bloqueados);
            lista.add(u);
        }
        return lista;
    }

    private List<Aviso> generarAvisos(int total, int noLeidos) {
        List<Aviso> lista = new ArrayList<>();
        User u = new User("X", "Y", "p", "x@deusto.es", User.Role.ESTUDIANTE);
        for (int i = 0; i < total; i++) {
            Aviso a = new Aviso(u, Aviso.TipoAviso.PENALIZACION, "T" + i, "M" + i);
            a.setLeido(i >= noLeidos);
            lista.add(a);
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