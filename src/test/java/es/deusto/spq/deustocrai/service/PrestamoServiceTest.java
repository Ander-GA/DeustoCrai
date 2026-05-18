package es.deusto.spq.deustocrai.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
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
import es.deusto.spq.deustocrai.entity.User;

import org.junit.jupiter.api.Tag;

@Tag("Unitario")
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
    
    @Test
    @DisplayName("obtenerEstadisticasUsuario: Debe calcular correctamente usando Map")
    void testObtenerEstadisticasUsuario() {
        User usuarioPrueba = new User();
        usuarioPrueba.setId(1L);

        // Préstamo 1: Activo
        Prestamo p1 = new Prestamo();
        p1.setEstado(Prestamo.EstadoPrestamo.PENDIENTE_ENTREGA);

        // Préstamo 2: Devuelto A TIEMPO
        Prestamo p2 = new Prestamo();
        p2.setEstado(Prestamo.EstadoPrestamo.DEVUELTO);
        p2.setFechaDevolucionReal(LocalDate.now().minusDays(1)); // Lo devolvió ayer
        p2.setFechaDevolucionPrevista(LocalDate.now()); // Tenía que devolverlo hoy

        // Préstamo 3: Devuelto CON RETRASO
        Prestamo p3 = new Prestamo();
        p3.setEstado(Prestamo.EstadoPrestamo.DEVUELTO);
        p3.setFechaDevolucionReal(LocalDate.now()); // Lo ha devuelto hoy
        p3.setFechaDevolucionPrevista(LocalDate.now().minusDays(1)); // Tenía que devolverlo ayer

        // Simulamos la respuesta de la base de datos
        when(prestamoRepository.findByUsuarioId(1L)).thenReturn(Arrays.asList(p1, p2, p3));

        // Ejecutamos el servicio
        Map<String, Integer> stats = prestamoService.obtenerEstadisticasUsuario(usuarioPrueba);

        // Verificamos que los números en el diccionario coinciden
        assertEquals(3, stats.get("totalPrestamos"));
        assertEquals(1, stats.get("prestamosActivos"));
        assertEquals(1, stats.get("devueltosATiempo"));
        assertEquals(1, stats.get("devueltosConRetraso"));
    }
     
}