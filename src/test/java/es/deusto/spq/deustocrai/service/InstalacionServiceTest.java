package es.deusto.spq.deustocrai.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.Tag;

import es.deusto.spq.deustocrai.dao.ReservaInstalacionRepository;
import es.deusto.spq.deustocrai.entity.InstalacionDeportiva;
import es.deusto.spq.deustocrai.entity.ReservaInstalacion;

@Tag("Unitario")
@ExtendWith(MockitoExtension.class)
public class InstalacionServiceTest {

    @Mock
    private ReservaInstalacionRepository reservaRepo;

    @InjectMocks
    private InstalacionService instalacionService;

    private ReservaInstalacion reservaNueva;
    private InstalacionDeportiva pista;

    @BeforeEach
    void setUp() {
        pista = new InstalacionDeportiva();
        ReflectionTestUtils.setField(pista, "id", 1L);

        reservaNueva = new ReservaInstalacion();
        reservaNueva.setInstalacion(pista);
    }

    // --- TESTS solicitarReserva ---

    @Test
    @DisplayName("solicitarReserva: Falla si la fecha es en el pasado")
    void testSolicitarReservaPasado() {
        // Simulamos una reserva de ayer
        LocalDateTime ayer = LocalDateTime.now().minusDays(1);
        reservaNueva.setFechaHoraInicio(ayer);
        reservaNueva.setFechaHoraFin(ayer.plusHours(1)); // Añadido para pasar la nueva validación de nulos
        
        String resultado = instalacionService.solicitarReserva(reservaNueva);
        
        assertEquals("No puedes reservar en el pasado.", resultado);
        verify(reservaRepo, never()).save(any());
    }

    @Test
    @DisplayName("solicitarReserva: Falla si se reserva con más de 6 días de antelación")
    void testSolicitarReservaMasDe6Dias() {
        // Simulamos una reserva para dentro de 10 días
        LocalDateTime futuro = LocalDateTime.now().plusDays(10);
        reservaNueva.setFechaHoraInicio(futuro);
        reservaNueva.setFechaHoraFin(futuro.plusHours(1)); // Añadido para pasar la nueva validación de nulos
        
        String resultado = instalacionService.solicitarReserva(reservaNueva);
        
        assertEquals("Solo puedes reservar con un máximo de 6 días de antelación.", resultado);
        verify(reservaRepo, never()).save(any());
    }

    @Test
    @DisplayName("solicitarReserva: Falla si ya existe una reserva aprobada que se solapa")
    void testSolicitarReservaSolapamiento() {
        // Reserva nueva: Mañana de 10:00 a 12:00
        LocalDateTime inicio = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        reservaNueva.setFechaHoraInicio(inicio);
        reservaNueva.setFechaHoraFin(inicio.plusHours(2));

        // Reserva existente (Aprobada): Mañana de 11:00 a 13:00 (Se pisan 1 hora)
        ReservaInstalacion existente = new ReservaInstalacion();
        existente.setFechaHoraInicio(inicio.plusHours(1));
        existente.setFechaHoraFin(inicio.plusHours(3));

        // CORRECCIÓN: Usamos el método con el guion bajo (_) que actualizamos en el repositorio
        when(reservaRepo.findByInstalacion_IdAndEstado(1L, ReservaInstalacion.EstadoReserva.APROBADA))
            .thenReturn(Arrays.asList(existente));

        String resultado = instalacionService.solicitarReserva(reservaNueva);

        assertEquals("La instalación ya está aprobada para otro usuario en ese horario.", resultado);
        verify(reservaRepo, never()).save(any());
    }

    @Test
    @DisplayName("solicitarReserva: Éxito si las fechas son correctas y no hay solapamiento")
    void testSolicitarReservaExito() {
        // Reserva válida para pasado mañana
        LocalDateTime inicio = LocalDateTime.now().plusDays(2).withHour(10);
        reservaNueva.setFechaHoraInicio(inicio);
        reservaNueva.setFechaHoraFin(inicio.plusHours(1));

        // CORRECCIÓN: Usamos el método con el guion bajo (_)
        when(reservaRepo.findByInstalacion_IdAndEstado(1L, ReservaInstalacion.EstadoReserva.APROBADA))
            .thenReturn(Collections.emptyList());

        String resultado = instalacionService.solicitarReserva(reservaNueva);

        assertEquals("OK", resultado);
        assertEquals(ReservaInstalacion.EstadoReserva.PENDIENTE, reservaNueva.getEstado());
        verify(reservaRepo, times(1)).save(reservaNueva);
    }

    // --- TESTS procesarSolicitud ---

    @Test
    @DisplayName("procesarSolicitud: Retorna true y guarda el nuevo estado si la reserva existe")
    void testProcesarSolicitudExito() {
        when(reservaRepo.findById(100L)).thenReturn(Optional.of(reservaNueva));

        boolean resultado = instalacionService.procesarSolicitud(100L, ReservaInstalacion.EstadoReserva.APROBADA);

        assertTrue(resultado);
        assertEquals(ReservaInstalacion.EstadoReserva.APROBADA, reservaNueva.getEstado());
        verify(reservaRepo, times(1)).save(reservaNueva);
    }

    @Test
    @DisplayName("procesarSolicitud: Retorna false si la reserva no existe")
    void testProcesarSolicitudFallo() {
        when(reservaRepo.findById(99L)).thenReturn(Optional.empty());

        boolean resultado = instalacionService.procesarSolicitud(99L, ReservaInstalacion.EstadoReserva.RECHAZADA);

        assertFalse(resultado);
        verify(reservaRepo, never()).save(any());
    }
}