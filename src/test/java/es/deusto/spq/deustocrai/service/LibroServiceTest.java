package es.deusto.spq.deustocrai.service;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.deusto.spq.deustocrai.dao.LibroRepository;
import es.deusto.spq.deustocrai.dao.PrestamoRepository;
import es.deusto.spq.deustocrai.entity.Libro;
import es.deusto.spq.deustocrai.entity.Prestamo;
import org.junit.jupiter.api.Tag;

@Tag("Unitario")
@ExtendWith(MockitoExtension.class)
public class LibroServiceTest {

    @Mock
    private LibroRepository libroRepository;

    @Mock
    private PrestamoRepository prestamoRepository;

    @InjectMocks
    private LibroService libroService;

    private Libro libro;

    @BeforeEach
    void setUp() {
        libro = new Libro("Clean Code", "978-0132350884", "Robert C. Martin");
        libro.setDisponible(true);
    }

    // ── anadirLibro ────────────────────────────────────────────────

    @Test
    @DisplayName("anadirLibro: guarda el libro y lo marca como disponible")
    void testAnadirLibroLoGuardaDisponible() {
        when(libroRepository.save(libro)).thenReturn(libro);

        Libro resultado = libroService.anadirLibro(libro);

        assertTrue(resultado.isDisponible());
        verify(libroRepository, times(1)).save(libro);
    }

    @Test
    @DisplayName("anadirLibro: devuelve el libro con los datos correctos")
    void testAnadirLibroDevuelveDatosCorrectos() {
        when(libroRepository.save(libro)).thenReturn(libro);

        Libro resultado = libroService.anadirLibro(libro);

        assertEquals("Clean Code", resultado.getTitulo());
        assertEquals("978-0132350884", resultado.getIsbn());
        assertEquals("Robert C. Martin", resultado.getAutor());
    }

    // ── borrarLibro ────────────────────────────────────────────────

    @Test
    @DisplayName("borrarLibro: devuelve -1 si el libro no existe")
    void testBorrarLibroNoExisteDevuelveMinusUno() {
        when(libroRepository.existsById(99L)).thenReturn(false);

        int resultado = libroService.borrarLibro(99L);

        assertEquals(-1, resultado);
        verify(libroRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("borrarLibro: devuelve 0 si el libro tiene un prestamo activo PENDIENTE_ENTREGA")
    void testBorrarLibroConPrestamoPendienteDevuelveCero() {
        Prestamo prestamo = mock(Prestamo.class);
        when(prestamo.getEstado()).thenReturn(Prestamo.EstadoPrestamo.PENDIENTE_ENTREGA);

        when(libroRepository.existsById(1L)).thenReturn(true);
        when(prestamoRepository.findByRecursoId(1L)).thenReturn(List.of(prestamo));

        int resultado = libroService.borrarLibro(1L);

        assertEquals(0, resultado);
        verify(libroRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("borrarLibro: devuelve 0 si el libro tiene un prestamo activo ENTREGADO")
    void testBorrarLibroConPrestamoEntregadoDevuelveCero() {
        Prestamo prestamo = mock(Prestamo.class);
        when(prestamo.getEstado()).thenReturn(Prestamo.EstadoPrestamo.ENTREGADO);

        when(libroRepository.existsById(1L)).thenReturn(true);
        when(prestamoRepository.findByRecursoId(1L)).thenReturn(List.of(prestamo));

        int resultado = libroService.borrarLibro(1L);

        assertEquals(0, resultado);
    }

    @Test
    @DisplayName("borrarLibro: borra correctamente si todos los prestamos estan DEVUELTOS")
    void testBorrarLibroConPrestamosDevueltosDevuelveUno() {
        Prestamo prestamo = mock(Prestamo.class);
        when(prestamo.getEstado()).thenReturn(Prestamo.EstadoPrestamo.DEVUELTO);

        when(libroRepository.existsById(1L)).thenReturn(true);
        when(prestamoRepository.findByRecursoId(1L)).thenReturn(List.of(prestamo));

        int resultado = libroService.borrarLibro(1L);

        assertEquals(1, resultado);
        verify(libroRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("borrarLibro: borra correctamente si no tiene ningun prestamo")
    void testBorrarLibroSinPrestamosDevuelveUno() {
        when(libroRepository.existsById(1L)).thenReturn(true);
        when(prestamoRepository.findByRecursoId(1L)).thenReturn(Collections.emptyList());

        int resultado = libroService.borrarLibro(1L);

        assertEquals(1, resultado);
        verify(libroRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("borrarLibro: desvincula el recurso de los prestamos historicos antes de borrar")
    void testBorrarLibroDesvincularPrestamosHistoricos() {
        Prestamo prestamo = mock(Prestamo.class);
        when(prestamo.getEstado()).thenReturn(Prestamo.EstadoPrestamo.DEVUELTO);

        when(libroRepository.existsById(1L)).thenReturn(true);
        when(prestamoRepository.findByRecursoId(1L)).thenReturn(List.of(prestamo));

        libroService.borrarLibro(1L);

        verify(prestamo, times(1)).setRecurso(null);
        verify(prestamoRepository, times(1)).saveAll(List.of(prestamo));
    }

    // ── listarLibros ───────────────────────────────────────────────

    @Test
    @DisplayName("listarLibros: devuelve todos los libros del repositorio")
    void testListarLibrosDevuelveLista() {
        Libro libro2 = new Libro("Effective Java", "978-0134685991", "Joshua Bloch");
        when(libroRepository.findAll()).thenReturn(Arrays.asList(libro, libro2));

        List<Libro> resultado = libroService.listarLibros();

        assertEquals(2, resultado.size());
        verify(libroRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("listarLibros: devuelve lista vacia si no hay libros")
    void testListarLibrosDevuelveListaVacia() {
        when(libroRepository.findAll()).thenReturn(Collections.emptyList());

        List<Libro> resultado = libroService.listarLibros();

        assertTrue(resultado.isEmpty());
    }

    // ── obtenerLibroPorId ──────────────────────────────────────────

    @Test
    @DisplayName("obtenerLibroPorId: devuelve el libro si existe")
    void testObtenerLibroPorIdExistente() {
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro));

        Optional<Libro> resultado = libroService.obtenerLibroPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Clean Code", resultado.get().getTitulo());
    }

    @Test
    @DisplayName("obtenerLibroPorId: devuelve Optional vacio si no existe")
    void testObtenerLibroPorIdNoExistente() {
        when(libroRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Libro> resultado = libroService.obtenerLibroPorId(99L);

        assertFalse(resultado.isPresent());
    }
}