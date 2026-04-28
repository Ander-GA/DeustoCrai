package es.deusto.spq.deustocrai.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

@ExtendWith(MockitoExtension.class)
public class PrestamoServiceTest {

    @Mock
    private PrestamoRepository prestamoRepository;

    @Mock
    private LibroRepository libroRepository;

    @InjectMocks
    private PrestamoService prestamoService;

    private Prestamo prestamo;
    private Libro libro;

    @BeforeEach
    void setUp() {
        libro = new Libro();
        libro.setDisponible(false); // Simula que está prestado

        prestamo = new Prestamo();
        prestamo.setRecurso(libro);
        prestamo.setEstado(Prestamo.EstadoPrestamo.PENDIENTE_ENTREGA);
    }

    @Test
    @DisplayName("cambiarEstadoPrestamo: Debería actualizar estado a ENTREGADO sin liberar libro")
    void testCambiarEstadoAEntregado() {
        when(prestamoRepository.findById(1L)).thenReturn(Optional.of(prestamo));

        boolean resultado = prestamoService.cambiarEstadoPrestamo(1L, Prestamo.EstadoPrestamo.ENTREGADO);

        assertTrue(resultado);
        assertEquals(Prestamo.EstadoPrestamo.ENTREGADO, prestamo.getEstado());
        assertFalse(libro.isDisponible()); // El libro sigue prestado
        verify(prestamoRepository, times(1)).save(prestamo);
    }

    @Test
    @DisplayName("cambiarEstadoPrestamo: Debería liberar el libro si el estado pasa a DEVUELTO")
    void testCambiarEstadoADevueltoLiberaLibro() {
        when(prestamoRepository.findById(1L)).thenReturn(Optional.of(prestamo));

        boolean resultado = prestamoService.cambiarEstadoPrestamo(1L, Prestamo.EstadoPrestamo.DEVUELTO);

        assertTrue(resultado);
        assertEquals(Prestamo.EstadoPrestamo.DEVUELTO, prestamo.getEstado());
        assertTrue(libro.isDisponible()); // El libro vuelve a estar disponible
        verify(libroRepository, times(1)).save(libro); // Se debe guardar el libro actualizado
    }
}