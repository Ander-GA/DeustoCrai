package es.deusto.spq.deustocrai.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.deusto.spq.deustocrai.dao.AvisoRepository;
import es.deusto.spq.deustocrai.dao.LibroRepository;
import es.deusto.spq.deustocrai.dao.MaterialRepository;
import es.deusto.spq.deustocrai.dao.PrestamoRepository;
import es.deusto.spq.deustocrai.dao.ReservaRepository;
import es.deusto.spq.deustocrai.dao.UserRepository;
import es.deusto.spq.deustocrai.entity.Aviso;
import es.deusto.spq.deustocrai.entity.Libro;
import es.deusto.spq.deustocrai.entity.Prestamo;
import es.deusto.spq.deustocrai.entity.User;

@Tag("Unitario")
@ExtendWith(MockitoExtension.class)
public class EstadisticasServiceTest {

    @Mock private PrestamoRepository prestamoRepository;
    @Mock private UserRepository userRepository;
    @Mock private LibroRepository libroRepository;
    @Mock private MaterialRepository materialRepository;
    @Mock private ReservaRepository reservaRepository;
    @Mock private AvisoRepository avisoRepository;

    @InjectMocks
    private EstadisticasService estadisticasService;

    private User usuario;
    private User usuarioBloqueado;

    @BeforeEach
    void setUp() {
        usuario = new User("Ana", "García", "pass", "ana@deusto.es", User.Role.ESTUDIANTE);
        usuario.setId(1L);
        usuario.setBloqueado(false);

        usuarioBloqueado = new User("Luis", "López", "pass", "luis@deusto.es", User.Role.ESTUDIANTE);
        usuarioBloqueado.setId(2L);
        usuarioBloqueado.setBloqueado(true);
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    /** Crea un préstamo activo (sin fecha de devolución real) con nombre histórico. */
    private Prestamo prestamoActivo(String nombreRecurso) {
        Prestamo p = new Prestamo();
        p.setNombreRecursoHistorico(nombreRecurso);
        p.setEstado(Prestamo.EstadoPrestamo.PENDIENTE_ENTREGA);
        // fechaDevolucionReal == null → activo
        return p;
    }

    /** Crea un préstamo ya devuelto (con fecha de devolución real). */
    private Prestamo prestamoDevuelto(String nombreRecurso) {
        Prestamo p = new Prestamo();
        p.setNombreRecursoHistorico(nombreRecurso);
        p.setEstado(Prestamo.EstadoPrestamo.DEVUELTO);
        p.setFechaDevolucionReal(LocalDate.now().minusDays(1));
        return p;
    }

    /** Crea un aviso marcado como leído o no leído. */
    private Aviso aviso(boolean leido) {
        User u = new User("X", "Y", "p", "x@deusto.es", User.Role.ESTUDIANTE);
        Aviso a = new Aviso(u, Aviso.TipoAviso.PENALIZACION, "Título", "Mensaje");
        a.setLeido(leido);
        return a;
    }

    // ─── obtenerResumen: contadores base ───────────────────────────────────────

    @Test
    @DisplayName("obtenerResumen: devuelve los contadores totales de repositorios")
    void testContadoresTotales() {
        when(prestamoRepository.count()).thenReturn(10L);
        when(userRepository.count()).thenReturn(5L);
        when(libroRepository.count()).thenReturn(20L);
        when(materialRepository.count()).thenReturn(8L);
        when(reservaRepository.count()).thenReturn(3L);
        when(avisoRepository.count()).thenReturn(7L);
        when(prestamoRepository.findAll()).thenReturn(Collections.emptyList());
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        when(avisoRepository.findAll()).thenReturn(Collections.emptyList());

        Map<String, Object> resumen = estadisticasService.obtenerResumen();

        assertEquals(10L, resumen.get("prestamosTotales"));
        assertEquals(5L, resumen.get("usuariosTotales"));
        assertEquals(20L, resumen.get("librosTotales"));
        assertEquals(8L, resumen.get("materialesTotales"));
        assertEquals(3L, resumen.get("reservasTotales"));
        assertEquals(7L, resumen.get("avisosTotales"));
    }

    @Test
    @DisplayName("obtenerResumen: todos los contadores son 0 con repositorios vacíos")
    void testContadoresCeroConRepositoriosVacios() {
        when(prestamoRepository.count()).thenReturn(0L);
        when(userRepository.count()).thenReturn(0L);
        when(libroRepository.count()).thenReturn(0L);
        when(materialRepository.count()).thenReturn(0L);
        when(reservaRepository.count()).thenReturn(0L);
        when(avisoRepository.count()).thenReturn(0L);
        when(prestamoRepository.findAll()).thenReturn(Collections.emptyList());
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        when(avisoRepository.findAll()).thenReturn(Collections.emptyList());

        Map<String, Object> resumen = estadisticasService.obtenerResumen();

        assertEquals(0L, resumen.get("prestamosTotales"));
        assertEquals(0L, resumen.get("prestamosActivos"));
        assertEquals(0L, resumen.get("usuariosPenalizados"));
        assertEquals(0L, resumen.get("avisosNoLeidos"));
    }

    // ─── obtenerResumen: prestamosActivos ──────────────────────────────────────

    @Test
    @DisplayName("obtenerResumen: cuenta correctamente los préstamos activos (sin devolución real)")
    void testPrestamosActivos() {
        Prestamo activo1 = prestamoActivo("El Quijote");
        Prestamo activo2 = prestamoActivo("Fundación");
        Prestamo devuelto = prestamoDevuelto("1984");

        stubMinimos();
        when(prestamoRepository.findAll()).thenReturn(Arrays.asList(activo1, activo2, devuelto));

        Map<String, Object> resumen = estadisticasService.obtenerResumen();

        assertEquals(2L, resumen.get("prestamosActivos"),
            "Solo los préstamos sin fechaDevolucionReal deben contarse como activos");
    }

    @Test
    @DisplayName("obtenerResumen: prestamosActivos es 0 cuando todos los préstamos están devueltos")
    void testPrestamosActivosCero() {
        stubMinimos();
        when(prestamoRepository.findAll()).thenReturn(
            Arrays.asList(prestamoDevuelto("Libro A"), prestamoDevuelto("Libro B"))
        );

        Map<String, Object> resumen = estadisticasService.obtenerResumen();

        assertEquals(0L, resumen.get("prestamosActivos"));
    }

    // ─── obtenerResumen: usuariosPenalizados ───────────────────────────────────

    @Test
    @DisplayName("obtenerResumen: cuenta correctamente los usuarios penalizados (bloqueado=true)")
    void testUsuariosPenalizados() {
        stubMinimos();
        when(userRepository.findAll()).thenReturn(Arrays.asList(usuario, usuarioBloqueado));

        Map<String, Object> resumen = estadisticasService.obtenerResumen();

        assertEquals(1L, resumen.get("usuariosPenalizados"));
    }

    @Test
    @DisplayName("obtenerResumen: usuariosPenalizados es 0 cuando nadie está bloqueado")
    void testUsuariosPenalizadosCero() {
        stubMinimos();
        when(userRepository.findAll()).thenReturn(Collections.singletonList(usuario));

        Map<String, Object> resumen = estadisticasService.obtenerResumen();

        assertEquals(0L, resumen.get("usuariosPenalizados"));
    }

    @Test
    @DisplayName("obtenerResumen: cuenta correctamente múltiples usuarios penalizados")
    void testVariosUsuariosPenalizados() {
        User u3 = new User("C", "D", "p", "c@deusto.es", User.Role.ESTUDIANTE);
        u3.setBloqueado(true);

        stubMinimos();
        when(userRepository.findAll()).thenReturn(Arrays.asList(usuario, usuarioBloqueado, u3));

        Map<String, Object> resumen = estadisticasService.obtenerResumen();

        assertEquals(2L, resumen.get("usuariosPenalizados"));
    }

    // ─── obtenerResumen: avisosNoLeidos ────────────────────────────────────────

    @Test
    @DisplayName("obtenerResumen: cuenta correctamente los avisos no leídos")
    void testAvisosNoLeidos() {
        stubMinimos();
        when(avisoRepository.findAll()).thenReturn(
            Arrays.asList(aviso(false), aviso(false), aviso(true))
        );

        Map<String, Object> resumen = estadisticasService.obtenerResumen();

        assertEquals(2L, resumen.get("avisosNoLeidos"));
    }

    @Test
    @DisplayName("obtenerResumen: avisosNoLeidos es 0 cuando todos los avisos están leídos")
    void testAvisosNoLeidosCero() {
        stubMinimos();
        when(avisoRepository.findAll()).thenReturn(
            Arrays.asList(aviso(true), aviso(true))
        );

        Map<String, Object> resumen = estadisticasService.obtenerResumen();

        assertEquals(0L, resumen.get("avisosNoLeidos"));
    }

    // ─── obtenerResumen: recursosSolicitados ───────────────────────────────────

    @Test
    @DisplayName("obtenerResumen: recursosSolicitados agrupa préstamos por nombre histórico")
    void testRecursosSolicitados_agrupaPorNombreHistorico() {
        Prestamo p1 = prestamoActivo("El Quijote");
        Prestamo p2 = prestamoActivo("El Quijote");
        Prestamo p3 = prestamoActivo("Fundación");

        stubMinimos();
        when(prestamoRepository.findAll()).thenReturn(Arrays.asList(p1, p2, p3));

        Map<String, Object> resumen = estadisticasService.obtenerResumen();

        @SuppressWarnings("unchecked")
        Map<String, Long> recursos = (Map<String, Long>) resumen.get("recursosSolicitados");

        assertEquals(2L, recursos.get("El Quijote"), "El Quijote debe tener 2 solicitudes");
        assertEquals(1L, recursos.get("Fundación"), "Fundación debe tener 1 solicitud");
    }

    @Test
    @DisplayName("obtenerResumen: recursosSolicitados usa getTitulo() del recurso si nombreHistorico es null")
    void testRecursosSolicitados_usaTituloRecursoCuandoHistoricoEsNull() {
        Libro libro = new Libro();
        libro.setTitulo("Dune");

        Prestamo p = new Prestamo();
        p.setNombreRecursoHistorico(null);
        p.setRecurso(libro);
        p.setEstado(Prestamo.EstadoPrestamo.PENDIENTE_ENTREGA);

        stubMinimos();
        when(prestamoRepository.findAll()).thenReturn(Collections.singletonList(p));

        Map<String, Object> resumen = estadisticasService.obtenerResumen();

        @SuppressWarnings("unchecked")
        Map<String, Long> recursos = (Map<String, Long>) resumen.get("recursosSolicitados");

        assertEquals(1L, recursos.get("Dune"), "Debe usar el título del recurso como fallback");
    }

    @Test
    @DisplayName("obtenerResumen: préstamo con nombreHistorico null y recurso null se omite")
    void testRecursosSolicitados_omitePrestamosSinNombre() {
        Prestamo p = new Prestamo();
        p.setNombreRecursoHistorico(null);
        p.setRecurso(null);
        p.setEstado(Prestamo.EstadoPrestamo.PENDIENTE_ENTREGA);

        stubMinimos();
        when(prestamoRepository.findAll()).thenReturn(Collections.singletonList(p));

        Map<String, Object> resumen = estadisticasService.obtenerResumen();

        @SuppressWarnings("unchecked")
        Map<String, Long> recursos = (Map<String, Long>) resumen.get("recursosSolicitados");

        assertTrue(recursos.isEmpty(),
            "Un préstamo sin nombre ni recurso no debe aparecer en recursosSolicitados");
    }

    @Test
    @DisplayName("obtenerResumen: recursosSolicitados está vacío si no hay préstamos")
    void testRecursosSolicitados_sinPrestamos() {
        stubMinimos();
        when(prestamoRepository.findAll()).thenReturn(Collections.emptyList());

        Map<String, Object> resumen = estadisticasService.obtenerResumen();

        @SuppressWarnings("unchecked")
        Map<String, Long> recursos = (Map<String, Long>) resumen.get("recursosSolicitados");

        assertTrue(recursos.isEmpty());
    }

    @Test
    @DisplayName("obtenerResumen: el mapa contiene exactamente las claves esperadas")
    void testResumenContieneTodasLasClaves() {
        stubMinimos();

        Map<String, Object> resumen = estadisticasService.obtenerResumen();

        List<String> clavesEsperadas = Arrays.asList(
            "prestamosTotales", "prestamosActivos", "usuariosTotales",
            "usuariosPenalizados", "librosTotales", "materialesTotales",
            "reservasTotales", "avisosTotales", "avisosNoLeidos", "recursosSolicitados"
        );

        for (String clave : clavesEsperadas) {
            assertTrue(resumen.containsKey(clave),
                "El resumen debe contener la clave: " + clave);
        }
    }

    private void stubMinimos() {
        when(prestamoRepository.count()).thenReturn(0L);
        when(userRepository.count()).thenReturn(0L);
        when(libroRepository.count()).thenReturn(0L);
        when(materialRepository.count()).thenReturn(0L);
        when(reservaRepository.count()).thenReturn(0L);
        when(avisoRepository.count()).thenReturn(0L);
        // Solo los que no se sobreescriben en el test que llama a stubMinimos:
        lenient().when(prestamoRepository.findAll()).thenReturn(Collections.emptyList());
        lenient().when(userRepository.findAll()).thenReturn(Collections.emptyList());
        lenient().when(avisoRepository.findAll()).thenReturn(Collections.emptyList());
    }
}