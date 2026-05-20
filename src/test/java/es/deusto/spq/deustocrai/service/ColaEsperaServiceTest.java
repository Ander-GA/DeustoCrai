package es.deusto.spq.deustocrai.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.deusto.spq.deustocrai.dao.AbstractRecursoRepository;
import es.deusto.spq.deustocrai.dao.ColaEsperaRepository;
import es.deusto.spq.deustocrai.dao.PrestamoRepository;
import es.deusto.spq.deustocrai.entity.AbstractRecurso;
import es.deusto.spq.deustocrai.entity.Aviso;
import es.deusto.spq.deustocrai.entity.ColaEspera;
import es.deusto.spq.deustocrai.entity.ColaEspera.EstadoCola;
import es.deusto.spq.deustocrai.entity.Libro;
import es.deusto.spq.deustocrai.entity.Prestamo;
import es.deusto.spq.deustocrai.entity.User;

@Tag("Unitario")
@ExtendWith(MockitoExtension.class)
public class ColaEsperaServiceTest {

    @Mock private ColaEsperaRepository colaEsperaRepository;
    @Mock private AbstractRecursoRepository recursoRepository;
    @Mock private PrestamoRepository prestamoRepository;
    @Mock private NotificacionService notificacionService;
    @Mock private AvisoService avisoService;

    @InjectMocks
    private ColaEsperaService colaEsperaService;

    private User usuario;
    private AbstractRecurso recurso;

    @BeforeEach
    void setUp() {
        usuario = new User();
        usuario.setId(1L);

        recurso = new Libro();
        recurso.setId(100L);
        recurso.setDisponible(false);
        recurso.setTitulo("Clean Code");
    }

    @Test
    @DisplayName("Apuntarse a cola - Éxito")
    void testApuntarseAColaSuccess() {
        when(recursoRepository.findById(100L)).thenReturn(Optional.of(recurso));
        when(colaEsperaRepository.existsByUsuarioIdAndRecursoIdAndEstado(1L, 100L, EstadoCola.ACTIVA)).thenReturn(false);
        
        ColaEspera expectedCola = new ColaEspera(usuario, recurso);
        when(colaEsperaRepository.save(any(ColaEspera.class))).thenReturn(expectedCola);

        ColaEspera result = colaEsperaService.apuntarseACola(usuario, 100L);

        assertNotNull(result);
        assertEquals(usuario, result.getUsuario());
        assertEquals(recurso, result.getRecurso());
    }

    @Test
    @DisplayName("Lanza excepción si recurso está disponible")
    void testApuntarseAColaDisponible() {
        recurso.setDisponible(true);
        when(recursoRepository.findById(100L)).thenReturn(Optional.of(recurso));

        assertThrows(IllegalStateException.class, () -> {
            colaEsperaService.apuntarseACola(usuario, 100L);
        });
    }

    @Test
    @DisplayName("Lanza excepción si ya está en la cola")
    void testApuntarseAColaDuplicado() {
        when(recursoRepository.findById(100L)).thenReturn(Optional.of(recurso));
        when(colaEsperaRepository.existsByUsuarioIdAndRecursoIdAndEstado(1L, 100L, EstadoCola.ACTIVA)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> {
            colaEsperaService.apuntarseACola(usuario, 100L);
        });
    }

    @Test
    @DisplayName("Asignar primer usuario - Si hay en cola, crea préstamo y notifica")
    void testAsignarPrimerUsuarioSiExiste() {
        ColaEspera primera = new ColaEspera(usuario, recurso);
        when(colaEsperaRepository.findFirstByRecursoIdAndEstadoOrderByFechaEntradaAsc(100L, EstadoCola.ACTIVA))
                .thenReturn(Optional.of(primera));

        Prestamo nuevoPrestamo = new Prestamo(usuario, recurso);
        when(prestamoRepository.save(any(Prestamo.class))).thenReturn(nuevoPrestamo);

        Optional<Prestamo> prestamo = colaEsperaService.asignarPrimerUsuarioSiExiste(recurso);

        assertTrue(prestamo.isPresent());
        assertEquals(EstadoCola.ASIGNADA, primera.getEstado());
        assertFalse(recurso.isDisponible());
        
        verify(notificacionService).notificarAsignacionDesdeCola(usuario, "Clean Code");
        verify(avisoService).crearAviso(eq(usuario), eq(Aviso.TipoAviso.COLA_ESPERA), anyString(), anyString());
    }

    @Test
    @DisplayName("Asignar primer usuario - Si no hay cola, pone el recurso disponible")
    void testAsignarPrimerUsuarioNoHayCola() {
        when(colaEsperaRepository.findFirstByRecursoIdAndEstadoOrderByFechaEntradaAsc(100L, EstadoCola.ACTIVA))
                .thenReturn(Optional.empty());

        Optional<Prestamo> prestamo = colaEsperaService.asignarPrimerUsuarioSiExiste(recurso);

        assertFalse(prestamo.isPresent());
        assertTrue(recurso.isDisponible());
        verify(recursoRepository).save(recurso);
    }

    @Test
    @DisplayName("Obtener posición exacta en la cola")
    void testObtenerPosicion() {
        User u2 = new User();
        u2.setId(2L);
        ColaEspera c1 = new ColaEspera(u2, recurso);
        ColaEspera c2 = new ColaEspera(usuario, recurso); // El que buscamos (pos 2)

        when(colaEsperaRepository.findByRecursoIdAndEstadoOrderByFechaEntradaAsc(100L, EstadoCola.ACTIVA))
                .thenReturn(Arrays.asList(c1, c2));

        int pos = colaEsperaService.obtenerPosicion(100L, 1L);
        assertEquals(2, pos);
    }

    @Test
    @DisplayName("Obtener posición - no está en la cola")
    void testObtenerPosicionFallo() {
        when(colaEsperaRepository.findByRecursoIdAndEstadoOrderByFechaEntradaAsc(100L, EstadoCola.ACTIVA))
                .thenReturn(Arrays.asList());

        int pos = colaEsperaService.obtenerPosicion(100L, 1L);
        assertEquals(-1, pos);
    }
}