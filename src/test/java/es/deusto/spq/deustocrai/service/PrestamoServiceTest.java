package es.deusto.spq.deustocrai.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.deusto.spq.deustocrai.dao.LibroRepository;
import es.deusto.spq.deustocrai.dao.MaterialRepository;
import es.deusto.spq.deustocrai.dao.PrestamoRepository;
import es.deusto.spq.deustocrai.entity.Libro;
import es.deusto.spq.deustocrai.entity.Material;
import es.deusto.spq.deustocrai.entity.Prestamo;
import es.deusto.spq.deustocrai.entity.User;

/**
 * Tests unitarios para PrestamoService.
 * Cubre todos los métodos públicos con Mockito.
 */
@ExtendWith(MockitoExtension.class)
class PrestamoServiceTest {

    @Mock
    private PrestamoRepository prestamoRepository;

    @Mock
    private LibroRepository libroRepository;

    @Mock
    private MaterialRepository materialRepository;
    
    @Mock
    private es.deusto.spq.deustocrai.service.ColaEsperaService colaEsperaService;

    @Mock
    private es.deusto.spq.deustocrai.dao.ColaEsperaRepository colaEsperaRepository;

    @InjectMocks
    private PrestamoService prestamoService;

    // ─── Fixtures ────────────────────────────────────────────────────────────────

    private User estudiante;
    private User bibliotecario;
    private Libro libroDisponible;
    private Libro libroNoDisponible;
    private Material materialDisponible;
    private Material materialNoDisponible;

    @BeforeEach
    void setUp() {
        estudiante    = new User("Ana", "García", "pass", "ana@deusto.es", User.Role.ESTUDIANTE);
        bibliotecario = new User("Pedro", "López", "bib", "pedro@deusto.es", User.Role.BIBLIOTECARIO);

        libroDisponible    = new Libro("El Señor de los Anillos", "978-001", "Tolkien");
        libroDisponible.setDisponible(true);

        libroNoDisponible  = new Libro("Harry Potter", "978-002", "Rowling");
        libroNoDisponible.setDisponible(false);

        materialDisponible    = new Material("Portátil Dell", "SN-001", "Portátil");
        materialDisponible.setDisponible(true);

        materialNoDisponible  = new Material("Cámara Canon", "SN-002", "Cámara");
        materialNoDisponible.setDisponible(false);
    }

    // ─── obtenerPrestamosPorUsuario ───────────────────────────────────────────────

    @Nested
    @DisplayName("obtenerPrestamosPorUsuario")
    class ObtenerPrestamosPorUsuarioTests {

        @Test
        @DisplayName("Filtra y devuelve sólo préstamos no devueltos")
        void testFiltrosolo_PrestamosActivos() {
            Prestamo activo   = buildPrestamo(estudiante, libroDisponible, Prestamo.EstadoPrestamo.PENDIENTE_ENTREGA);
            Prestamo devuelto = buildPrestamo(estudiante, libroDisponible, Prestamo.EstadoPrestamo.DEVUELTO);

            when(prestamoRepository.findByUsuarioId(estudiante.getId()))
                    .thenReturn(Arrays.asList(activo, devuelto));

            List<Prestamo> resultado = prestamoService.obtenerPrestamosPorUsuario(estudiante);

            assertEquals(1, resultado.size());
            assertNotEquals(Prestamo.EstadoPrestamo.DEVUELTO, resultado.get(0).getEstado());
        }

        @Test
        @DisplayName("Incluye préstamos en estado ENTREGADO (activo, no devuelto)")
        void testIncluyeEntregado() {
            Prestamo entregado = buildPrestamo(estudiante, libroDisponible, Prestamo.EstadoPrestamo.ENTREGADO);
            when(prestamoRepository.findByUsuarioId(estudiante.getId()))
                    .thenReturn(List.of(entregado));

            List<Prestamo> resultado = prestamoService.obtenerPrestamosPorUsuario(estudiante);

            assertEquals(1, resultado.size());
        }

        @Test
        @DisplayName("Todos devueltos: la lista resultante está vacía")
        void testTodosDevueltos() {
            Prestamo d1 = buildPrestamo(estudiante, libroDisponible, Prestamo.EstadoPrestamo.DEVUELTO);
            Prestamo d2 = buildPrestamo(estudiante, libroDisponible, Prestamo.EstadoPrestamo.DEVUELTO);
            when(prestamoRepository.findByUsuarioId(estudiante.getId()))
                    .thenReturn(Arrays.asList(d1, d2));

            List<Prestamo> resultado = prestamoService.obtenerPrestamosPorUsuario(estudiante);

            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Sin préstamos: devuelve lista vacía")
        void testSinPrestamos() {
            when(prestamoRepository.findByUsuarioId(estudiante.getId()))
                    .thenReturn(Collections.emptyList());

            List<Prestamo> resultado = prestamoService.obtenerPrestamosPorUsuario(estudiante);

            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Llama al repositorio con el ID del usuario correcto")
        void testLlamaRepositorioConIdUsuario() {
            estudiante.setId(42L);
            when(prestamoRepository.findByUsuarioId(42L)).thenReturn(Collections.emptyList());

            prestamoService.obtenerPrestamosPorUsuario(estudiante);

            verify(prestamoRepository).findByUsuarioId(42L);
        }

        @Test
        @DisplayName("Varios activos: los devuelve todos")
        void testVariosActivos() {
            Prestamo p1 = buildPrestamo(estudiante, libroDisponible, Prestamo.EstadoPrestamo.PENDIENTE_ENTREGA);
            Prestamo p2 = buildPrestamo(estudiante, libroDisponible, Prestamo.EstadoPrestamo.ENTREGADO);
            when(prestamoRepository.findByUsuarioId(estudiante.getId()))
                    .thenReturn(Arrays.asList(p1, p2));

            List<Prestamo> resultado = prestamoService.obtenerPrestamosPorUsuario(estudiante);

            assertEquals(2, resultado.size());
        }
    }

    // ─── obtenerTodosLosPrestamos ─────────────────────────────────────────────────

    @Nested
    @DisplayName("obtenerTodosLosPrestamos")
    class ObtenerTodosLosPrestamosTests {

        @Test
        @DisplayName("Devuelve todos los préstamos sin filtrar")
        void testDevuelveTodos() {
            Prestamo p1 = buildPrestamo(estudiante, libroDisponible, Prestamo.EstadoPrestamo.PENDIENTE_ENTREGA);
            Prestamo p2 = buildPrestamo(estudiante, libroDisponible, Prestamo.EstadoPrestamo.DEVUELTO);
            when(prestamoRepository.findAll()).thenReturn(Arrays.asList(p1, p2));

            List<Prestamo> resultado = prestamoService.obtenerTodosLosPrestamos();

            assertEquals(2, resultado.size());
        }

        @Test
        @DisplayName("Devuelve lista vacía si no hay préstamos")
        void testDevuelveVaciaSinPrestamos() {
            when(prestamoRepository.findAll()).thenReturn(Collections.emptyList());

            List<Prestamo> resultado = prestamoService.obtenerTodosLosPrestamos();

            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Llama a findAll exactamente una vez")
        void testLlamaFindAllUnaVez() {
            when(prestamoRepository.findAll()).thenReturn(Collections.emptyList());

            prestamoService.obtenerTodosLosPrestamos();

            verify(prestamoRepository, times(1)).findAll();
        }
    }

    // ─── realizarPrestamo ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("realizarPrestamo (libro)")
    class RealizarPrestamoTests {

        @Test
        @DisplayName("Libro disponible: devuelve el nuevo préstamo")
        void testPrestamoLibroDisponible() {
            when(libroRepository.findById(1L)).thenReturn(Optional.of(libroDisponible));
            when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(inv -> inv.getArgument(0));

            Prestamo resultado = prestamoService.realizarPrestamo(estudiante, 1L);

            assertNotNull(resultado);
        }

        @Test
        @DisplayName("Libro disponible: el préstamo tiene estado PENDIENTE_ENTREGA")
        void testPrestamoLibroEstadoInicial() {
            when(libroRepository.findById(1L)).thenReturn(Optional.of(libroDisponible));
            when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(inv -> inv.getArgument(0));

            Prestamo resultado = prestamoService.realizarPrestamo(estudiante, 1L);

            assertEquals(Prestamo.EstadoPrestamo.PENDIENTE_ENTREGA, resultado.getEstado());
        }

        @Test
        @DisplayName("Libro disponible: el libro queda marcado como no disponible")
        void testPrestamoLibroMarcaComoNoDisponible() {
            when(libroRepository.findById(1L)).thenReturn(Optional.of(libroDisponible));
            when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(inv -> inv.getArgument(0));

            prestamoService.realizarPrestamo(estudiante, 1L);

            assertFalse(libroDisponible.isDisponible(), "El libro debe quedar no disponible tras el préstamo");
        }

        @Test
        @DisplayName("Libro disponible: llama a libroRepository.save para actualizar disponibilidad")
        void testPrestamoLibroGuardaLibro() {
            when(libroRepository.findById(1L)).thenReturn(Optional.of(libroDisponible));
            when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(inv -> inv.getArgument(0));

            prestamoService.realizarPrestamo(estudiante, 1L);

            verify(libroRepository, times(1)).save(libroDisponible);
        }

        @Test
        @DisplayName("Libro no disponible: devuelve null")
        void testPrestamoLibroNoDisponible() {
            when(libroRepository.findById(2L)).thenReturn(Optional.of(libroNoDisponible));

            Prestamo resultado = prestamoService.realizarPrestamo(estudiante, 2L);

            assertNull(resultado);
        }

        @Test
        @DisplayName("Libro no existente: devuelve null")
        void testPrestamoLibroNoExiste() {
            when(libroRepository.findById(99L)).thenReturn(Optional.empty());

            Prestamo resultado = prestamoService.realizarPrestamo(estudiante, 99L);

            assertNull(resultado);
        }

        @Test
        @DisplayName("Libro no disponible: no llama a prestamoRepository.save")
        void testPrestamoLibroNoDisponibleNoGuardaPrestamo() {
            when(libroRepository.findById(2L)).thenReturn(Optional.of(libroNoDisponible));

            prestamoService.realizarPrestamo(estudiante, 2L);

            verify(prestamoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Préstamo exitoso: la fecha de préstamo es hoy")
        void testPrestamoFechaHoy() {
            when(libroRepository.findById(1L)).thenReturn(Optional.of(libroDisponible));
            when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(inv -> inv.getArgument(0));

            Prestamo resultado = prestamoService.realizarPrestamo(estudiante, 1L);

            assertEquals(LocalDate.now(), resultado.getFechaPrestamo());
        }

        @Test
        @DisplayName("Préstamo exitoso: la fecha prevista de devolución es en 7 días")
        void testPrestamoFechaDevolucion() {
            when(libroRepository.findById(1L)).thenReturn(Optional.of(libroDisponible));
            when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(inv -> inv.getArgument(0));

            Prestamo resultado = prestamoService.realizarPrestamo(estudiante, 1L);

            assertEquals(LocalDate.now().plusDays(7), resultado.getFechaDevolucionPrevista());
        }

        @Test
        @DisplayName("Préstamo exitoso: el snapshot de nombre histórico es el título del libro")
        void testPrestamoGuardaNombreHistorico() {
            when(libroRepository.findById(1L)).thenReturn(Optional.of(libroDisponible));
            when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(inv -> inv.getArgument(0));

            Prestamo resultado = prestamoService.realizarPrestamo(estudiante, 1L);

            assertEquals("El Señor de los Anillos", resultado.getNombreRecursoHistorico());
        }
    }

    // ─── realizarPrestamoMaterial ─────────────────────────────────────────────────

    @Nested
    @DisplayName("realizarPrestamoMaterial")
    class RealizarPrestamoMaterialTests {

        @Test
        @DisplayName("Material disponible: devuelve el nuevo préstamo")
        void testPrestamoMaterialDisponible() {
            when(materialRepository.findById(1L)).thenReturn(Optional.of(materialDisponible));
            when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(inv -> inv.getArgument(0));

            Prestamo resultado = prestamoService.realizarPrestamoMaterial(estudiante, 1L);

            assertNotNull(resultado);
        }

        @Test
        @DisplayName("Material disponible: el material queda marcado como no disponible")
        void testPrestamoMaterialMarcaNoDisponible() {
            when(materialRepository.findById(1L)).thenReturn(Optional.of(materialDisponible));
            when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(inv -> inv.getArgument(0));

            prestamoService.realizarPrestamoMaterial(estudiante, 1L);

            assertFalse(materialDisponible.isDisponible());
        }

        @Test
        @DisplayName("Material disponible: el préstamo tiene estado PENDIENTE_ENTREGA")
        void testPrestamoMaterialEstadoInicial() {
            when(materialRepository.findById(1L)).thenReturn(Optional.of(materialDisponible));
            when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(inv -> inv.getArgument(0));

            Prestamo resultado = prestamoService.realizarPrestamoMaterial(estudiante, 1L);

            assertEquals(Prestamo.EstadoPrestamo.PENDIENTE_ENTREGA, resultado.getEstado());
        }

        @Test
        @DisplayName("Material no disponible: devuelve null")
        void testPrestamoMaterialNoDisponible() {
            when(materialRepository.findById(2L)).thenReturn(Optional.of(materialNoDisponible));

            Prestamo resultado = prestamoService.realizarPrestamoMaterial(estudiante, 2L);

            assertNull(resultado);
        }

        @Test
        @DisplayName("Material no existente: devuelve null")
        void testPrestamoMaterialNoExiste() {
            when(materialRepository.findById(99L)).thenReturn(Optional.empty());

            Prestamo resultado = prestamoService.realizarPrestamoMaterial(estudiante, 99L);

            assertNull(resultado);
        }

        @Test
        @DisplayName("Material no disponible: no llama a prestamoRepository.save")
        void testPrestamoMaterialNoDisponibleNoGuardaPrestamo() {
            when(materialRepository.findById(2L)).thenReturn(Optional.of(materialNoDisponible));

            prestamoService.realizarPrestamoMaterial(estudiante, 2L);

            verify(prestamoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Material disponible: el snapshot de nombre histórico es el título del material")
        void testPrestamoMaterialGuardaNombreHistorico() {
            when(materialRepository.findById(1L)).thenReturn(Optional.of(materialDisponible));
            when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(inv -> inv.getArgument(0));

            Prestamo resultado = prestamoService.realizarPrestamoMaterial(estudiante, 1L);

            assertEquals("Portátil Dell", resultado.getNombreRecursoHistorico());
        }

        @Test
        @DisplayName("Material disponible: llama a materialRepository.save para actualizar disponibilidad")
        void testPrestamoMaterialGuardaMaterial() {
            when(materialRepository.findById(1L)).thenReturn(Optional.of(materialDisponible));
            when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(inv -> inv.getArgument(0));

            prestamoService.realizarPrestamoMaterial(estudiante, 1L);

            verify(materialRepository, times(1)).save(materialDisponible);
        }
    }

    // ─── devolverPrestamo ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("devolverPrestamo")
    class DevolverPrestamoTests {

    	@Test
        @DisplayName("Devolución exitosa de libro: devuelve true")
        void testDevolverLibroExitoso() {
            estudiante.setId(1L);
            Prestamo prestamo = buildPrestamo(estudiante, libroNoDisponible, Prestamo.EstadoPrestamo.ENTREGADO);

            when(prestamoRepository.findById(10L)).thenReturn(Optional.of(prestamo));
            // Eliminamos el mock innecesario de libroRepository.save
            when(prestamoRepository.save(any(Prestamo.class))).thenReturn(prestamo);

            boolean resultado = prestamoService.devolverPrestamo(estudiante, 10L);

            assertTrue(resultado);
        }

        @Test
        @DisplayName("Devolución exitosa de libro: avisa a colaEsperaService")
        void testDevolverLibroQuedaDisponible() {
            estudiante.setId(1L);
            Prestamo prestamo = buildPrestamo(estudiante, libroNoDisponible, Prestamo.EstadoPrestamo.ENTREGADO);

            when(prestamoRepository.findById(10L)).thenReturn(Optional.of(prestamo));
            when(prestamoRepository.save(any(Prestamo.class))).thenReturn(prestamo);

            prestamoService.devolverPrestamo(estudiante, 10L);

            // Ahora verificamos que avisa a la cola de espera en lugar de asertar la disponibilidad
            verify(colaEsperaService, times(1)).asignarPrimerUsuarioSiExiste(libroNoDisponible);
        }

        @Test
        @DisplayName("Devolución exitosa: el préstamo pasa a estado DEVUELTO")
        void testDevolverLibroCambiaEstado() {
            estudiante.setId(1L);
            Prestamo prestamo = buildPrestamo(estudiante, libroNoDisponible, Prestamo.EstadoPrestamo.ENTREGADO);

            when(prestamoRepository.findById(10L)).thenReturn(Optional.of(prestamo));
            when(prestamoRepository.save(any(Prestamo.class))).thenReturn(prestamo);

            prestamoService.devolverPrestamo(estudiante, 10L);

            assertEquals(Prestamo.EstadoPrestamo.DEVUELTO, prestamo.getEstado());
        }

        @Test
        @DisplayName("Préstamo no encontrado: devuelve false")
        void testDevolverPrestamoNoExistente() {
            when(prestamoRepository.findById(99L)).thenReturn(Optional.empty());

            boolean resultado = prestamoService.devolverPrestamo(estudiante, 99L);

            assertFalse(resultado);
        }

        @Test
        @DisplayName("Devolución por usuario incorrecto: devuelve false")
        void testDevolverPrestamoUsuarioIncorrecto() {
            User otro = new User("Otro", "User", "pw", "otro@deusto.es", User.Role.ESTUDIANTE);
            otro.setId(99L);
            estudiante.setId(1L);

            Prestamo prestamo = buildPrestamo(estudiante, libroNoDisponible, Prestamo.EstadoPrestamo.ENTREGADO);

            when(prestamoRepository.findById(10L)).thenReturn(Optional.of(prestamo));

            boolean resultado = prestamoService.devolverPrestamo(otro, 10L);

            assertFalse(resultado);
        }

        @Test
        @DisplayName("Préstamo ya devuelto: devuelve false")
        void testDevolverPrestamoYaDevuelto() {
            estudiante.setId(1L);
            Prestamo prestamo = buildPrestamo(estudiante, libroNoDisponible, Prestamo.EstadoPrestamo.DEVUELTO);

            when(prestamoRepository.findById(10L)).thenReturn(Optional.of(prestamo));

            boolean resultado = prestamoService.devolverPrestamo(estudiante, 10L);

            assertFalse(resultado);
        }

        @Test
        @DisplayName("Devolución exitosa de material: el material pasa a control de calidad (no disponible)")
        void testDevolverMaterialQuedaDisponible() {
            estudiante.setId(1L);
            materialNoDisponible.setDisponible(false);
            Prestamo prestamo = buildPrestamo(estudiante, materialNoDisponible, Prestamo.EstadoPrestamo.ENTREGADO);

            when(prestamoRepository.findById(20L)).thenReturn(Optional.of(prestamo));
            when(materialRepository.save(any(Material.class))).thenReturn(materialNoDisponible);
            when(prestamoRepository.save(any(Prestamo.class))).thenReturn(prestamo);

            boolean resultado = prestamoService.devolverPrestamo(estudiante, 20L);

            assertTrue(resultado);
            assertFalse(materialNoDisponible.isDisponible());
        }
    }

    // ─── cambiarEstadoPrestamo ────────────────────────────────────────────────────

    @Nested
    @DisplayName("cambiarEstadoPrestamo")
    class CambiarEstadoPrestamoTests {

        @Test
        @DisplayName("Préstamo existente: cambia el estado y devuelve true")
        void testCambiarEstadoExitoso() {
            Prestamo prestamo = buildPrestamo(estudiante, libroNoDisponible, Prestamo.EstadoPrestamo.PENDIENTE_ENTREGA);
            when(prestamoRepository.findById(1L)).thenReturn(Optional.of(prestamo));
            when(prestamoRepository.save(any(Prestamo.class))).thenReturn(prestamo);

            boolean resultado = prestamoService.cambiarEstadoPrestamo(1L, Prestamo.EstadoPrestamo.ENTREGADO);

            assertTrue(resultado);
            assertEquals(Prestamo.EstadoPrestamo.ENTREGADO, prestamo.getEstado());
        }

        @Test
        @DisplayName("Préstamo no existente: devuelve false")
        void testCambiarEstadoNoExiste() {
            when(prestamoRepository.findById(99L)).thenReturn(Optional.empty());

            boolean resultado = prestamoService.cambiarEstadoPrestamo(99L, Prestamo.EstadoPrestamo.ENTREGADO);

            assertFalse(resultado);
        }

        @Test
        @DisplayName("Cambiar a DEVUELTO: llama a colaEsperaService para gestionar reservas")
        void testCambiarEstadoDevueltoLiberaLibro() {
            libroNoDisponible.setDisponible(false);
            Prestamo prestamo = buildPrestamo(estudiante, libroNoDisponible, Prestamo.EstadoPrestamo.ENTREGADO);

            when(prestamoRepository.findById(1L)).thenReturn(Optional.of(prestamo));
            when(prestamoRepository.save(any(Prestamo.class))).thenReturn(prestamo);

            prestamoService.cambiarEstadoPrestamo(1L, Prestamo.EstadoPrestamo.DEVUELTO);

            // Verificamos que delega en el sistema de colas
            verify(colaEsperaService, times(1)).asignarPrimerUsuarioSiExiste(libroNoDisponible);
        }

        @Test
        @DisplayName("Cambiar a DEVUELTO con material: el material pasa a control de calidad (no disponible)")
        void testCambiarEstadoDevueltoLiberaMaterial() {
            materialNoDisponible.setDisponible(false);
            Prestamo prestamo = buildPrestamo(estudiante, materialNoDisponible, Prestamo.EstadoPrestamo.ENTREGADO);

            when(prestamoRepository.findById(1L)).thenReturn(Optional.of(prestamo));
            when(prestamoRepository.save(any(Prestamo.class))).thenReturn(prestamo);
            when(materialRepository.save(any(Material.class))).thenReturn(materialNoDisponible);

            prestamoService.cambiarEstadoPrestamo(1L, Prestamo.EstadoPrestamo.DEVUELTO);

            assertFalse(materialNoDisponible.isDisponible());
        }

        @Test
        @DisplayName("Cambiar a PENDIENTE_ENTREGA: no libera el libro")
        void testCambiarEstadoPendienteEntregaNoLiberaLibro() {
            libroNoDisponible.setDisponible(false);
            Prestamo prestamo = buildPrestamo(estudiante, libroNoDisponible, Prestamo.EstadoPrestamo.PENDIENTE_ENTREGA);

            when(prestamoRepository.findById(1L)).thenReturn(Optional.of(prestamo));
            when(prestamoRepository.save(any(Prestamo.class))).thenReturn(prestamo);

            prestamoService.cambiarEstadoPrestamo(1L, Prestamo.EstadoPrestamo.ENTREGADO);

            assertFalse(libroNoDisponible.isDisponible());
            verify(libroRepository, never()).save(any());
        }

        @Test
        @DisplayName("Cambiar estado: llama a prestamoRepository.save exactamente una vez")
        void testCambiarEstadoLlamaSaveUnaVez() {
            Prestamo prestamo = buildPrestamo(estudiante, libroNoDisponible, Prestamo.EstadoPrestamo.PENDIENTE_ENTREGA);
            when(prestamoRepository.findById(1L)).thenReturn(Optional.of(prestamo));
            when(prestamoRepository.save(any(Prestamo.class))).thenReturn(prestamo);

            prestamoService.cambiarEstadoPrestamo(1L, Prestamo.EstadoPrestamo.ENTREGADO);

            verify(prestamoRepository, times(1)).save(prestamo);
        }

        @Test
        @DisplayName("Cambiar a DEVUELTO: NO guarda el libro directamente")
        void testCambiarEstadoDevueltoGuardaLibro() {
            libroNoDisponible.setDisponible(false);
            Prestamo prestamo = buildPrestamo(estudiante, libroNoDisponible, Prestamo.EstadoPrestamo.ENTREGADO);

            when(prestamoRepository.findById(1L)).thenReturn(Optional.of(prestamo));
            when(prestamoRepository.save(any(Prestamo.class))).thenReturn(prestamo);

            prestamoService.cambiarEstadoPrestamo(1L, Prestamo.EstadoPrestamo.DEVUELTO);

            // Verificamos que el repositorio de libros YA NO es llamado aquí
            verify(libroRepository, never()).save(any());
        }
    }

    // ─── obtenerPrestamosLibrosActivos ────────────────────────────────────────────

    @Nested
    @DisplayName("obtenerPrestamosLibrosActivos")
    class ObtenerPrestamosLibrosActivosTests {

        @Test
        @DisplayName("Devuelve la lista del repositorio sin modificar")
        void testDevuelveLista() {
            Prestamo p1 = buildPrestamo(estudiante, libroDisponible, Prestamo.EstadoPrestamo.PENDIENTE_ENTREGA);
            when(prestamoRepository.findLibrosPrestadosActivos()).thenReturn(List.of(p1));

            List<Prestamo> resultado = prestamoService.obtenerPrestamosLibrosActivos();

            assertEquals(1, resultado.size());
        }

        @Test
        @DisplayName("Lista vacía cuando no hay préstamos activos de libros")
        void testDevuelveVaciaSinActivos() {
            when(prestamoRepository.findLibrosPrestadosActivos()).thenReturn(Collections.emptyList());

            List<Prestamo> resultado = prestamoService.obtenerPrestamosLibrosActivos();

            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Llama a findLibrosPrestadosActivos exactamente una vez")
        void testLlamaRepositorioUnaVez() {
            when(prestamoRepository.findLibrosPrestadosActivos()).thenReturn(Collections.emptyList());

            prestamoService.obtenerPrestamosLibrosActivos();

            verify(prestamoRepository, times(1)).findLibrosPrestadosActivos();
        }
    }

    // ─── Helper ───────────────────────────────────────────────────────────────────

    private Prestamo buildPrestamo(User user, es.deusto.spq.deustocrai.entity.AbstractRecurso recurso, Prestamo.EstadoPrestamo estado) {
        Prestamo p = new Prestamo(user, recurso);
        p.setEstado(estado);
        return p;
    }
}