package es.deusto.spq.deustocrai.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
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

import es.deusto.spq.deustocrai.dao.ReservaRepository;
import es.deusto.spq.deustocrai.entity.Aula;
import es.deusto.spq.deustocrai.entity.Reserva;
import es.deusto.spq.deustocrai.entity.User;
import org.junit.jupiter.api.Tag;

@Tag("Unitario")
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
public class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private ReservaService reservaService;

    private Aula aula;
    private User usuario;
    private LocalDateTime inicio;
    private LocalDateTime fin;

    @BeforeEach
    void setUp() {
        aula = mock(Aula.class);
        when(aula.getId()).thenReturn(1L);

        usuario = mock(User.class);

        inicio = LocalDateTime.of(2025, 6, 1, 10, 0);
        fin    = LocalDateTime.of(2025, 6, 1, 12, 0);
    }

    // ── realizarReserva: sin conflicto ─────────────────────────────

    @Test
    @DisplayName("realizarReserva: guarda la reserva si el aula esta libre")
    void testRealizarReservaSinConflicto() {
        Reserva nueva = new Reserva(usuario, aula, inicio, fin);
        when(reservaRepository.findByAulaId(1L)).thenReturn(Collections.emptyList());
        when(reservaRepository.save(nueva)).thenReturn(nueva);

        Optional<Reserva> resultado = reservaService.realizarReserva(nueva);

        assertTrue(resultado.isPresent());
        verify(reservaRepository, times(1)).save(nueva);
    }

    @Test
    @DisplayName("realizarReserva: la reserva guardada tiene los datos correctos")
    void testRealizarReservaDatosCorrectos() {
        Reserva nueva = new Reserva(usuario, aula, inicio, fin);
        when(reservaRepository.findByAulaId(1L)).thenReturn(Collections.emptyList());
        when(reservaRepository.save(nueva)).thenReturn(nueva);

        Optional<Reserva> resultado = reservaService.realizarReserva(nueva);

        assertEquals(inicio, resultado.get().getFechaHoraInicio());
        assertEquals(fin, resultado.get().getFechaHoraFin());
    }

    // ── realizarReserva: con conflicto ─────────────────────────────

    @Test
    @DisplayName("realizarReserva: devuelve Optional vacio si hay solapamiento exacto")
    void testRealizarReservaConSolapamientoExacto() {
        Reserva existente = new Reserva(usuario, aula, inicio, fin);
        Reserva nueva     = new Reserva(usuario, aula, inicio, fin);

        when(reservaRepository.findByAulaId(1L)).thenReturn(List.of(existente));

        Optional<Reserva> resultado = reservaService.realizarReserva(nueva);

        assertTrue(resultado.isEmpty());
        verify(reservaRepository, never()).save(any());
    }

    @Test
    @DisplayName("realizarReserva: devuelve Optional vacio si la nueva empieza dentro de una existente")
    void testRealizarReservaEmpiezaDentroDeExistente() {
        Reserva existente = new Reserva(usuario, aula,
            LocalDateTime.of(2025, 6, 1, 9, 0),
            LocalDateTime.of(2025, 6, 1, 11, 0));

        Reserva nueva = new Reserva(usuario, aula,
            LocalDateTime.of(2025, 6, 1, 10, 30),
            LocalDateTime.of(2025, 6, 1, 12, 0));

        when(reservaRepository.findByAulaId(1L)).thenReturn(List.of(existente));

        Optional<Reserva> resultado = reservaService.realizarReserva(nueva);

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("realizarReserva: devuelve Optional vacio si la nueva engloba una existente")
    void testRealizarReservaEnglobaExistente() {
        Reserva existente = new Reserva(usuario, aula,
            LocalDateTime.of(2025, 6, 1, 10, 30),
            LocalDateTime.of(2025, 6, 1, 11, 30));

        Reserva nueva = new Reserva(usuario, aula,
            LocalDateTime.of(2025, 6, 1, 10, 0),
            LocalDateTime.of(2025, 6, 1, 12, 0));

        when(reservaRepository.findByAulaId(1L)).thenReturn(List.of(existente));

        Optional<Reserva> resultado = reservaService.realizarReserva(nueva);

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("realizarReserva: permite reserva que empieza justo cuando termina la existente")
    void testRealizarReservaAdyacenteNoConflicto() {
        Reserva existente = new Reserva(usuario, aula,
            LocalDateTime.of(2025, 6, 1, 8, 0),
            LocalDateTime.of(2025, 6, 1, 10, 0));

        Reserva nueva = new Reserva(usuario, aula,
            LocalDateTime.of(2025, 6, 1, 10, 0),
            LocalDateTime.of(2025, 6, 1, 12, 0));

        when(reservaRepository.findByAulaId(1L)).thenReturn(List.of(existente));
        when(reservaRepository.save(nueva)).thenReturn(nueva);

        Optional<Reserva> resultado = reservaService.realizarReserva(nueva);

        assertTrue(resultado.isPresent());
    }

    // ── getReservasPorAula ─────────────────────────────────────────

    @Test
    @DisplayName("getReservasPorAula: devuelve las reservas del aula indicada")
    void testGetReservasPorAula() {
        Reserva r = new Reserva(usuario, aula, inicio, fin);
        when(reservaRepository.findByAulaId(1L)).thenReturn(List.of(r));

        List<Reserva> resultado = reservaService.getReservasPorAula(1L);

        assertEquals(1, resultado.size());
        verify(reservaRepository, times(1)).findByAulaId(1L);
    }

    @Test
    @DisplayName("getReservasPorAula: devuelve lista vacia si el aula no tiene reservas")
    void testGetReservasPorAulaSinReservas() {
        when(reservaRepository.findByAulaId(2L)).thenReturn(Collections.emptyList());

        List<Reserva> resultado = reservaService.getReservasPorAula(2L);

        assertTrue(resultado.isEmpty());
    }

    // ── obtenerReservasActivas ─────────────────────────────────────

    @Test
    @DisplayName("obtenerReservasActivas: devuelve reservas futuras")
    void testObtenerReservasActivas() {
        Reserva activa = new Reserva(usuario, aula,
            LocalDateTime.now().plusHours(1),
            LocalDateTime.now().plusHours(3));

        when(reservaRepository.findReservasActivas()).thenReturn(List.of(activa));

        List<Reserva> resultado = reservaService.obtenerReservasActivas();

        assertEquals(1, resultado.size());
        verify(reservaRepository, times(1)).findReservasActivas();
    }

    @Test
    @DisplayName("obtenerReservasActivas: devuelve lista vacia si no hay reservas activas")
    void testObtenerReservasActivasVacia() {
        when(reservaRepository.findReservasActivas()).thenReturn(Collections.emptyList());

        List<Reserva> resultado = reservaService.obtenerReservasActivas();

        assertTrue(resultado.isEmpty());
    }
}
