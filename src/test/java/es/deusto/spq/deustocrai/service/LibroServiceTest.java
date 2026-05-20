package es.deusto.spq.deustocrai.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

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
import es.deusto.spq.deustocrai.dao.PrestamoRepository;
import es.deusto.spq.deustocrai.entity.Libro;
import es.deusto.spq.deustocrai.entity.Prestamo;
import es.deusto.spq.deustocrai.entity.User;

/**
 * Tests unitarios para LibroService.
 * Cubre anadirLibro, borrarLibro, listarLibros y obtenerLibroPorId.
 */
@ExtendWith(MockitoExtension.class)
class LibroServiceTest {

    @Mock
    private LibroRepository libroRepository;

    @Mock
    private PrestamoRepository prestamoRepository;

    @InjectMocks
    private LibroService libroService;

    // ─── Fixtures ────────────────────────────────────────────────────────────────

    private Libro libroDisponible;
    private Libro libroNuevo;

    @BeforeEach
    void setUp() {
        libroDisponible = new Libro("Don Quijote", "978-8491050742", "Cervantes");
        libroNuevo = new Libro("1984", "978-0451524935", "Orwell");
    }

    // ─── anadirLibro ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("anadirLibro")
    class AnadirLibroTests {

        @Test
        @DisplayName("Añadir libro: devuelve el libro guardado con disponible=true")
        void testAnadirLibroDevuelveLibroGuardado() {
            libroNuevo.setDisponible(false); // simulamos que llega como false
            when(libroRepository.save(any(Libro.class))).thenAnswer(inv -> inv.getArgument(0));

            Libro resultado = libroService.anadirLibro(libroNuevo);

            assertNotNull(resultado);
            assertTrue(resultado.isDisponible(), "El libro siempre debe guardarse como disponible");
        }

        @Test
        @DisplayName("Añadir libro: fuerza disponible=true independientemente del valor inicial")
        void testAnadirLibroFuerzaDisponibleTrue() {
            libroNuevo.setDisponible(false);
            when(libroRepository.save(any(Libro.class))).thenAnswer(inv -> inv.getArgument(0));

            libroService.anadirLibro(libroNuevo);

            ArgumentCaptor<Libro> captor = ArgumentCaptor.forClass(Libro.class);
            verify(libroRepository).save(captor.capture());
            assertTrue(captor.getValue().isDisponible());
        }

        @Test
        @DisplayName("Añadir libro: llama a save exactamente una vez")
        void testAnadirLibroLlamaSaveUnaVez() {
            when(libroRepository.save(any(Libro.class))).thenAnswer(inv -> inv.getArgument(0));

            libroService.anadirLibro(libroNuevo);

            verify(libroRepository, times(1)).save(any(Libro.class));
        }

        @Test
        @DisplayName("Añadir libro: conserva el título original")
        void testAnadirLibroConservaTitulo() {
            when(libroRepository.save(any(Libro.class))).thenAnswer(inv -> inv.getArgument(0));

            Libro resultado = libroService.anadirLibro(libroNuevo);

            assertEquals("1984", resultado.getTitulo());
        }

        @Test
        @DisplayName("Añadir libro: conserva el ISBN original")
        void testAnadirLibroConservaIsbn() {
            when(libroRepository.save(any(Libro.class))).thenAnswer(inv -> inv.getArgument(0));

            Libro resultado = libroService.anadirLibro(libroNuevo);

            assertEquals("978-0451524935", resultado.getIsbn());
        }

        @Test
        @DisplayName("Añadir libro: conserva el autor original")
        void testAnadirLibroConservaAutor() {
            when(libroRepository.save(any(Libro.class))).thenAnswer(inv -> inv.getArgument(0));

            Libro resultado = libroService.anadirLibro(libroNuevo);

            assertEquals("Orwell", resultado.getAutor());
        }

        @Test
        @DisplayName("Añadir libro: el repositorio recibe exactamente el mismo objeto")
        void testAnadirLibroRepositorioRecibeElMismoObjeto() {
            when(libroRepository.save(libroNuevo)).thenReturn(libroNuevo);

            libroService.anadirLibro(libroNuevo);

            verify(libroRepository).save(libroNuevo);
        }
    }

    // ─── borrarLibro ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("borrarLibro")
    class BorrarLibroTests {

        @Test
        @DisplayName("Borrar libro que no existe: devuelve -1 (404)")
        void testBorrarLibroNoExiste() {
            when(libroRepository.existsById(99L)).thenReturn(false);

            int resultado = libroService.borrarLibro(99L);

            assertEquals(-1, resultado);
        }

        @Test
        @DisplayName("Borrar libro no existente: no llama a findByRecursoId")
        void testBorrarLibroNoExistenteNoConsultaPrestamos() {
            when(libroRepository.existsById(99L)).thenReturn(false);

            libroService.borrarLibro(99L);

            verify(prestamoRepository, never()).findByRecursoId(anyLong());
        }

        @Test
        @DisplayName("Borrar libro prestado activamente (PENDIENTE_ENTREGA): devuelve 0 (409)")
        void testBorrarLibroPrestadoPendienteEntrega() {
            Prestamo prestamo = crearPrestamo(Prestamo.EstadoPrestamo.PENDIENTE_ENTREGA);
            when(libroRepository.existsById(1L)).thenReturn(true);
            when(prestamoRepository.findByRecursoId(1L)).thenReturn(List.of(prestamo));

            int resultado = libroService.borrarLibro(1L);

            assertEquals(0, resultado);
        }

        @Test
        @DisplayName("Borrar libro en estado ENTREGADO: devuelve 0 (409)")
        void testBorrarLibroPrestadoEntregado() {
            Prestamo prestamo = crearPrestamo(Prestamo.EstadoPrestamo.ENTREGADO);
            when(libroRepository.existsById(1L)).thenReturn(true);
            when(prestamoRepository.findByRecursoId(1L)).thenReturn(List.of(prestamo));

            int resultado = libroService.borrarLibro(1L);

            assertEquals(0, resultado);
        }

        @Test
        @DisplayName("Borrar libro con todos los préstamos DEVUELTOS: devuelve 1 (204)")
        void testBorrarLibroConPrestamosDevueltos() {
            Prestamo prestamo = crearPrestamo(Prestamo.EstadoPrestamo.DEVUELTO);
            when(libroRepository.existsById(1L)).thenReturn(true);
            when(prestamoRepository.findByRecursoId(1L)).thenReturn(List.of(prestamo));

            int resultado = libroService.borrarLibro(1L);

            assertEquals(1, resultado);
        }

        @Test
        @DisplayName("Borrar libro sin préstamos históricos: devuelve 1 (204)")
        void testBorrarLibroSinPrestamos() {
            when(libroRepository.existsById(2L)).thenReturn(true);
            when(prestamoRepository.findByRecursoId(2L)).thenReturn(Collections.emptyList());

            int resultado = libroService.borrarLibro(2L);

            assertEquals(1, resultado);
        }

        @Test
        @DisplayName("Borrar libro exitoso: llama a deleteById una vez")
        void testBorrarLibroLlamaDeleteById() {
            when(libroRepository.existsById(2L)).thenReturn(true);
            when(prestamoRepository.findByRecursoId(2L)).thenReturn(Collections.emptyList());

            libroService.borrarLibro(2L);

            verify(libroRepository, times(1)).deleteById(2L);
        }

        @Test
        @DisplayName("Borrar libro con préstamos devueltos: desvincula el recurso (setRecurso null) antes de borrar")
        void testBorrarLibroDesvincularRecurso() {
            Libro libro = new Libro("Linked", "000", "Autor");
            Prestamo prestamo = crearPrestamo(Prestamo.EstadoPrestamo.DEVUELTO);
            prestamo.setRecurso(libro);

            when(libroRepository.existsById(1L)).thenReturn(true);
            when(prestamoRepository.findByRecursoId(1L)).thenReturn(List.of(prestamo));

            libroService.borrarLibro(1L);

            assertNull(prestamo.getRecurso(), "El recurso debe desvincularse antes del borrado");
        }

        @Test
        @DisplayName("Borrar libro con préstamos devueltos: guarda los préstamos desvinculados")
        void testBorrarLibroGuardaPrestamosDesvinculados() {
            Prestamo prestamo = crearPrestamo(Prestamo.EstadoPrestamo.DEVUELTO);
            when(libroRepository.existsById(1L)).thenReturn(true);
            when(prestamoRepository.findByRecursoId(1L)).thenReturn(List.of(prestamo));

            libroService.borrarLibro(1L);

            verify(prestamoRepository, times(1)).saveAll(anyList());
        }

        @Test
        @DisplayName("Borrar libro prestado: NO llama a deleteById")
        void testBorrarLibroPrestadoNoLlamaDelete() {
            Prestamo prestamo = crearPrestamo(Prestamo.EstadoPrestamo.PENDIENTE_ENTREGA);
            when(libroRepository.existsById(1L)).thenReturn(true);
            when(prestamoRepository.findByRecursoId(1L)).thenReturn(List.of(prestamo));

            libroService.borrarLibro(1L);

            verify(libroRepository, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("Borrar libro con varios préstamos: un PENDIENTE hace que devuelva 0")
        void testBorrarLibroMixtoConUnPendiente() {
            Prestamo devuelto = crearPrestamo(Prestamo.EstadoPrestamo.DEVUELTO);
            Prestamo pendiente = crearPrestamo(Prestamo.EstadoPrestamo.PENDIENTE_ENTREGA);
            when(libroRepository.existsById(1L)).thenReturn(true);
            when(prestamoRepository.findByRecursoId(1L)).thenReturn(Arrays.asList(devuelto, pendiente));

            int resultado = libroService.borrarLibro(1L);

            assertEquals(0, resultado);
        }

        // Helper
        private Prestamo crearPrestamo(Prestamo.EstadoPrestamo estado) {
            User user = new User("Test", "User", "pw", "t@t.es", User.Role.ESTUDIANTE);
            Prestamo p = new Prestamo(user, libroDisponible);
            p.setEstado(estado);
            return p;
        }
    }

    // ─── listarLibros ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("listarLibros")
    class ListarLibrosTests {

        @Test
        @DisplayName("Devuelve la lista completa del repositorio")
        void testListarLibrosDevuelveLista() {
            List<Libro> libros = Arrays.asList(libroDisponible, libroNuevo);
            when(libroRepository.findAll()).thenReturn(libros);

            List<Libro> resultado = libroService.listarLibros();

            assertEquals(2, resultado.size());
        }

        @Test
        @DisplayName("Devuelve lista vacía cuando no hay libros")
        void testListarLibrosVacio() {
            when(libroRepository.findAll()).thenReturn(Collections.emptyList());

            List<Libro> resultado = libroService.listarLibros();

            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Llama a findAll exactamente una vez")
        void testListarLibrosLlamaFindAll() {
            when(libroRepository.findAll()).thenReturn(Collections.emptyList());

            libroService.listarLibros();

            verify(libroRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Los libros devueltos contienen los títulos correctos")
        void testListarLibrosTitulosCorrectos() {
            when(libroRepository.findAll()).thenReturn(Arrays.asList(libroDisponible, libroNuevo));

            List<Libro> resultado = libroService.listarLibros();

            assertTrue(resultado.stream().anyMatch(l -> "Don Quijote".equals(l.getTitulo())));
            assertTrue(resultado.stream().anyMatch(l -> "1984".equals(l.getTitulo())));
        }

        @Test
        @DisplayName("Devuelve la misma referencia de lista que el repositorio")
        void testListarLibrosMismaReferencia() {
            List<Libro> libros = List.of(libroDisponible);
            when(libroRepository.findAll()).thenReturn(libros);

            List<Libro> resultado = libroService.listarLibros();

            assertSame(libros, resultado);
        }
    }

    // ─── obtenerLibroPorId ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("obtenerLibroPorId")
    class ObtenerLibroPorIdTests {

        @Test
        @DisplayName("ID existente: devuelve Optional con el libro")
        void testObtenerPorIdExistente() {
            when(libroRepository.findById(1L)).thenReturn(Optional.of(libroDisponible));

            Optional<Libro> resultado = libroService.obtenerLibroPorId(1L);

            assertTrue(resultado.isPresent());
            assertEquals("Don Quijote", resultado.get().getTitulo());
        }

        @Test
        @DisplayName("ID inexistente: devuelve Optional vacío")
        void testObtenerPorIdNoExistente() {
            when(libroRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<Libro> resultado = libroService.obtenerLibroPorId(999L);

            assertFalse(resultado.isPresent());
        }

        @Test
        @DisplayName("Llama al repositorio con el ID recibido")
        void testObtenerPorIdLlamaRepositorio() {
            when(libroRepository.findById(42L)).thenReturn(Optional.empty());

            libroService.obtenerLibroPorId(42L);

            verify(libroRepository).findById(42L);
        }

        @Test
        @DisplayName("El libro devuelto tiene el autor correcto")
        void testObtenerPorIdAutorCorrecto() {
            when(libroRepository.findById(1L)).thenReturn(Optional.of(libroDisponible));

            Libro resultado = libroService.obtenerLibroPorId(1L).get();

            assertEquals("Cervantes", resultado.getAutor());
        }

        @Test
        @DisplayName("El libro devuelto tiene el ISBN correcto")
        void testObtenerPorIdIsbnCorrecto() {
            when(libroRepository.findById(1L)).thenReturn(Optional.of(libroDisponible));

            Libro resultado = libroService.obtenerLibroPorId(1L).get();

            assertEquals("978-8491050742", resultado.getIsbn());
        }

        @Test
        @DisplayName("Devuelve Optional de libro disponible correctamente")
        void testObtenerPorIdLibroDisponible() {
            libroDisponible.setDisponible(true);
            when(libroRepository.findById(1L)).thenReturn(Optional.of(libroDisponible));

            Libro resultado = libroService.obtenerLibroPorId(1L).get();

            assertTrue(resultado.isDisponible());
        }

        @Test
        @DisplayName("Devuelve Optional de libro no disponible correctamente")
        void testObtenerPorIdLibroNoDisponible() {
            libroDisponible.setDisponible(false);
            when(libroRepository.findById(1L)).thenReturn(Optional.of(libroDisponible));

            Libro resultado = libroService.obtenerLibroPorId(1L).get();

            assertFalse(resultado.isDisponible());
        }
    }
}