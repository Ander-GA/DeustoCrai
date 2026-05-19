package es.deusto.spq.deustocrai.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Tag;

import es.deusto.spq.deustocrai.dao.LibroRepository;
import es.deusto.spq.deustocrai.dao.PrestamoRepository;
import es.deusto.spq.deustocrai.entity.Libro;
import es.deusto.spq.deustocrai.entity.Prestamo;

@Tag("Unitario")
@ExtendWith(MockitoExtension.class)
public class LibroServiceTest {

    @Mock
    private LibroRepository libroRepository;

    @Mock
    private PrestamoRepository prestamoRepository;

    @InjectMocks
    private LibroService libroService;

    private Libro libroMock;

    @BeforeEach
    void setUp() {
        libroMock = new Libro();
        org.springframework.test.util.ReflectionTestUtils.setField(libroMock, "id", 1L);
        libroMock.setTitulo("Cien Años de Soledad");
    }

    @Test
    @DisplayName("anadirLibro: Se guarda marcándose como disponible")
    void testAnadirLibro() {
        when(libroRepository.save(any(Libro.class))).thenReturn(libroMock);

        Libro resultado = libroService.anadirLibro(libroMock);

        assertTrue(resultado.isDisponible());
        verify(libroRepository, times(1)).save(libroMock);
    }

    @Test
    @DisplayName("borrarLibro: Retorna -1 si el libro no existe en la BD")
    void testBorrarLibroNoExiste() {
        when(libroRepository.existsById(1L)).thenReturn(false);

        int resultado = libroService.borrarLibro(1L);

        assertEquals(-1, resultado);
        // Nos aseguramos de que no sigue ejecutando lógica innecesaria
        verify(prestamoRepository, never()).findByRecursoId(anyLong()); 
    }

    @Test
    @DisplayName("borrarLibro: Retorna 0 (Conflicto) si un alumno tiene el libro sin devolver")
    void testBorrarLibroPrestado() {
        when(libroRepository.existsById(1L)).thenReturn(true);
        
        // Simulamos un préstamo que aún no se ha devuelto
        Prestamo prestamoActivo = new Prestamo();
        prestamoActivo.setEstado(Prestamo.EstadoPrestamo.PENDIENTE_ENTREGA);
        
        when(prestamoRepository.findByRecursoId(1L)).thenReturn(Arrays.asList(prestamoActivo));

        int resultado = libroService.borrarLibro(1L);

        assertEquals(0, resultado);
        verify(libroRepository, never()).deleteById(anyLong()); // Comprobamos que no se ha borrado
    }

    @Test
    @DisplayName("borrarLibro: Retorna 1, desvincula el historial y borra con éxito")
    void testBorrarLibroExito() {
        when(libroRepository.existsById(1L)).thenReturn(true);
        
        // Simulamos que existe un historial antiguo, pero el libro ya está devuelto
        Prestamo prestamoHistorico = new Prestamo();
        prestamoHistorico.setEstado(Prestamo.EstadoPrestamo.DEVUELTO);
        prestamoHistorico.setRecurso(libroMock);
        
        when(prestamoRepository.findByRecursoId(1L)).thenReturn(Arrays.asList(prestamoHistorico));

        int resultado = libroService.borrarLibro(1L);

        assertEquals(1, resultado);
        assertNull(prestamoHistorico.getRecurso()); // Verifica que se cortó la cuerda correctamente
        verify(prestamoRepository, times(1)).saveAll(anyList());
        verify(libroRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("listarLibros: Devuelve la lista de la BD")
    void testListarLibros() {
        when(libroRepository.findAll()).thenReturn(Arrays.asList(libroMock));

        List<Libro> resultado = libroService.listarLibros();

        assertEquals(1, resultado.size());
        verify(libroRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("obtenerLibroPorId: Devuelve el Optional correcto")
    void testObtenerLibroPorId() {
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libroMock));

        Optional<Libro> resultado = libroService.obtenerLibroPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Cien Años de Soledad", resultado.get().getTitulo());
    }
}