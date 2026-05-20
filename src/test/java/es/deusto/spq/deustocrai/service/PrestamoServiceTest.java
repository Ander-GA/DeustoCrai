package es.deusto.spq.deustocrai.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.deusto.spq.deustocrai.dao.ColaEsperaRepository;
import es.deusto.spq.deustocrai.dao.LibroRepository;
import es.deusto.spq.deustocrai.dao.MaterialRepository;
import es.deusto.spq.deustocrai.dao.PrestamoRepository;
import es.deusto.spq.deustocrai.entity.ColaEspera;
import es.deusto.spq.deustocrai.entity.Libro;
import es.deusto.spq.deustocrai.entity.Material;
import es.deusto.spq.deustocrai.entity.Prestamo;
import es.deusto.spq.deustocrai.entity.User;

@Tag("Unitario")
@ExtendWith(MockitoExtension.class)
public class PrestamoServiceTest {

    @Mock
    private PrestamoRepository prestamoRepository;

    @Mock
    private LibroRepository libroRepository;

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private ColaEsperaService colaEsperaService;

    // --- AÑADIDO PARA LOS NUEVOS TESTS ---
    @Mock
    private ColaEsperaRepository colaEsperaRepository;

    @InjectMocks
    private PrestamoService prestamoService;

    // --- VARIABLES ORIGINALES (COMPAÑERO) ---
    private Prestamo prestamo;
    private Libro libro;

    // --- NUEVAS VARIABLES ---
    private User usuario;
    private Libro libroDisponible;
    private Material material;
    private Prestamo prestamoLibro;
    private Prestamo prestamoMaterial;

    @BeforeEach
    void setUp() {
        // --- INICIALIZACIÓN COMPAÑERO ---
        libro = new Libro();
        org.springframework.test.util.ReflectionTestUtils.setField(libro, "id", 1L);
        libro.setDisponible(false); // Simula que está prestado

        prestamo = new Prestamo();
        prestamo.setRecurso(libro);
        prestamo.setEstado(Prestamo.EstadoPrestamo.PENDIENTE_ENTREGA);

        // --- INICIALIZACIÓN NUEVOS TESTS ---
        usuario = new User();
        usuario.setId(1L);

        libroDisponible = new Libro();
        org.springframework.test.util.ReflectionTestUtils.setField(libroDisponible, "id", 100L);
        libroDisponible.setDisponible(true);

        material = new Material();
        org.springframework.test.util.ReflectionTestUtils.setField(material, "id", 200L);
        material.setDisponible(true);

        prestamoLibro = new Prestamo(usuario, libroDisponible);
        org.springframework.test.util.ReflectionTestUtils.setField(prestamoLibro, "id", 50L);

        prestamoMaterial = new Prestamo(usuario, material);
        org.springframework.test.util.ReflectionTestUtils.setField(prestamoMaterial, "id", 60L);
    }

    // ==========================================
    //        TESTS ORIGINALES (COMPAÑERO)
    // ==========================================

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
        verify(colaEsperaService, times(1)).asignarPrimerUsuarioSiExiste(libro);
        verify(prestamoRepository, times(1)).save(prestamo);
    }
    
    @Test
    @DisplayName("obtenerEstadisticasUsuario: Debe calcular correctamente usando Map")
    void testObtenerEstadisticasUsuario() {
        User usuarioPrueba = new User();
        usuarioPrueba.setId(1L);

        Prestamo p1 = new Prestamo();
        p1.setEstado(Prestamo.EstadoPrestamo.PENDIENTE_ENTREGA);

        Prestamo p2 = new Prestamo();
        p2.setEstado(Prestamo.EstadoPrestamo.DEVUELTO);
        p2.setFechaDevolucionReal(LocalDate.now().minusDays(1)); 
        p2.setFechaDevolucionPrevista(LocalDate.now()); 

        Prestamo p3 = new Prestamo();
        p3.setEstado(Prestamo.EstadoPrestamo.DEVUELTO);
        p3.setFechaDevolucionReal(LocalDate.now()); 
        p3.setFechaDevolucionPrevista(LocalDate.now().minusDays(1)); 

        when(prestamoRepository.findByUsuarioId(1L)).thenReturn(Arrays.asList(p1, p2, p3));

        Map<String, Integer> stats = prestamoService.obtenerEstadisticasUsuario(usuarioPrueba);

        assertEquals(3, stats.get("totalPrestamos"));
        assertEquals(1, stats.get("prestamosActivos"));
        assertEquals(1, stats.get("devueltosATiempo"));
        assertEquals(1, stats.get("devueltosConRetraso"));
    }

    // ==========================================
    //        NUEVOS TESTS PARA COMPLETAR JACOCO
    // ==========================================

    @Test
    @DisplayName("Realizar préstamo de libro - Éxito")
    void testRealizarPrestamoLibroExito() {
        when(libroRepository.findById(100L)).thenReturn(Optional.of(libroDisponible));
        when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(i -> i.getArgument(0));

        Prestamo result = prestamoService.realizarPrestamo(usuario, 100L);

        assertNotNull(result);
        assertFalse(libroDisponible.isDisponible());
        verify(libroRepository).save(libroDisponible);
    }

    @Test
    @DisplayName("Realizar préstamo de libro - Falla si no está disponible")
    void testRealizarPrestamoLibroNoDisponible() {
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro)); // 'libro' ya tiene disponible=false

        Prestamo result = prestamoService.realizarPrestamo(usuario, 1L);
        assertNull(result);
    }

    @Test
    @DisplayName("Realizar préstamo de material tecnológico - Éxito")
    void testRealizarPrestamoMaterialExito() {
        when(materialRepository.findById(200L)).thenReturn(Optional.of(material));
        when(prestamoRepository.save(any(Prestamo.class))).thenAnswer(i -> i.getArgument(0));

        Prestamo result = prestamoService.realizarPrestamoMaterial(usuario, 200L);

        assertNotNull(result);
        assertFalse(material.isDisponible());
        verify(materialRepository).save(material);
    }

    @Test
    @DisplayName("Devolver préstamo Libro de forma explícita")
    void testDevolverPrestamoLibro() {
        when(prestamoRepository.findById(50L)).thenReturn(Optional.of(prestamoLibro));
        when(prestamoRepository.save(any(Prestamo.class))).thenReturn(prestamoLibro);

        boolean exito = prestamoService.devolverPrestamo(usuario, 50L);

        assertTrue(exito);
        assertEquals(Prestamo.EstadoPrestamo.DEVUELTO, prestamoLibro.getEstado());
        verify(colaEsperaService).asignarPrimerUsuarioSiExiste(libroDisponible);
    }

    @Test
    @DisplayName("Devolver préstamo Material - Pasa a no disponible para revisión (Control de Calidad)")
    void testDevolverPrestamoMaterial() {
        when(prestamoRepository.findById(60L)).thenReturn(Optional.of(prestamoMaterial));
        
        boolean exito = prestamoService.devolverPrestamo(usuario, 60L);

        assertTrue(exito);
        assertFalse(material.isDisponible());
        verify(materialRepository).save(material);
    }

    @Test
    @DisplayName("Devolver préstamo - Falla por ser de otro usuario")
    void testDevolverPrestamoOtroUsuario() {
        User otro = new User();
        otro.setId(99L);
        when(prestamoRepository.findById(50L)).thenReturn(Optional.of(prestamoLibro));

        boolean exito = prestamoService.devolverPrestamo(otro, 50L);
        assertFalse(exito);
    }

    @Test
    @DisplayName("Cambiar estado préstamo - Devolver Material (Para Admin)")
    void testCambiarEstadoPrestamoMaterial() {
        when(prestamoRepository.findById(60L)).thenReturn(Optional.of(prestamoMaterial));

        boolean exito = prestamoService.cambiarEstadoPrestamo(60L, Prestamo.EstadoPrestamo.DEVUELTO);

        assertTrue(exito);
        assertFalse(material.isDisponible());
        verify(materialRepository).save(material);
    }

    @Test
    @DisplayName("Renovar préstamo - Éxito")
    void testRenovarPrestamoExito() {
        prestamoLibro.setFechaDevolucionPrevista(LocalDate.now().plusDays(2));
        when(prestamoRepository.findById(50L)).thenReturn(Optional.of(prestamoLibro));
        when(colaEsperaRepository.findByRecursoIdAndEstadoOrderByFechaEntradaAsc(100L, ColaEspera.EstadoCola.ACTIVA))
                .thenReturn(Collections.emptyList());
        when(prestamoRepository.save(any())).thenReturn(prestamoLibro);

        Prestamo result = prestamoService.renovarPrestamo(50L, usuario);
        
        assertNotNull(result);
        assertEquals(LocalDate.now().plusDays(9), result.getFechaDevolucionPrevista());
    }

    @Test
    @DisplayName("Renovar préstamo - Lanza excepción si hay gente en cola de espera")
    void testRenovarPrestamoColaLlena() {
        prestamoLibro.setFechaDevolucionPrevista(LocalDate.now().plusDays(2));
        when(prestamoRepository.findById(50L)).thenReturn(Optional.of(prestamoLibro));
        
        ColaEspera espera = new ColaEspera(new User(), libroDisponible);
        when(colaEsperaRepository.findByRecursoIdAndEstadoOrderByFechaEntradaAsc(100L, ColaEspera.EstadoCola.ACTIVA))
                .thenReturn(Arrays.asList(espera));

        assertThrows(IllegalStateException.class, () -> prestamoService.renovarPrestamo(50L, usuario));
    }

    @Test
    @DisplayName("Renovar préstamo - Lanza excepción si ya venció")
    void testRenovarPrestamoVencido() {
        prestamoLibro.setFechaDevolucionPrevista(LocalDate.now().minusDays(1)); // Caducado
        when(prestamoRepository.findById(50L)).thenReturn(Optional.of(prestamoLibro));

        assertThrows(IllegalStateException.class, () -> prestamoService.renovarPrestamo(50L, usuario));
    }

    @Test
    @DisplayName("Verifica listados y filtros de historial")
    void testListadosBasicos() {
        when(prestamoRepository.findLibrosPrestadosActivos()).thenReturn(Arrays.asList(prestamoLibro));
        when(prestamoRepository.findMaterialesPrestadosActivos()).thenReturn(Arrays.asList(prestamoMaterial));
        
        assertEquals(1, prestamoService.obtenerPrestamosLibrosActivos().size());
        assertEquals(1, prestamoService.obtenerPrestamosMaterialesActivos().size());
    }
}