package es.deusto.spq.deustocrai.service;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import java.time.LocalDateTime;
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

import es.deusto.spq.deustocrai.dao.ReservaRepository;
import es.deusto.spq.deustocrai.dao.BloqueoSalaRepository;
import es.deusto.spq.deustocrai.dao.UserRepository;
import es.deusto.spq.deustocrai.service.EmailService;
import es.deusto.spq.deustocrai.entity.Aula;
import es.deusto.spq.deustocrai.entity.Reserva;
import es.deusto.spq.deustocrai.entity.User;

/**
 * Tests unitarios para ReservaService.
 * Cubre getReservasPorAula, realizarReserva y obtenerReservasActivas.
 */
@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;
    @Mock
    private es.deusto.spq.deustocrai.dao.BloqueoSalaRepository bloqueoSalaRepository;

    @Mock
    private es.deusto.spq.deustocrai.dao.UserRepository userRepository;

    @Mock
    private es.deusto.spq.deustocrai.service.EmailService emailService;

    @InjectMocks
    private ReservaService reservaService;

    // ─── Fixtures ────────────────────────────────────────────────────────────────

    private User usuario;
    private Aula aula;

    // Base temporal: mañana a las 10:00
    private LocalDateTime base;

    @BeforeEach
    void setUp() {
        usuario = new User("Ana", "García", "pass", "ana@deusto.es", User.Role.ESTUDIANTE);
        aula = new Aula("Sala Juntas", 10, true);
        aula.setId(1L);

        base = LocalDateTime.now()
                .plusDays(1)
                .withHour(10).withMinute(0).withSecond(0).withNano(0);
                
       
        lenient().when(bloqueoSalaRepository.findByAulaId(any())).thenReturn(Collections.emptyList());
    }

    // ─── getReservasPorAula ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getReservasPorAula")
    class GetReservasPorAulaTests {

        @Test
        @DisplayName("Devuelve la lista de reservas del repositorio para ese aula")
        void testDevuelveLista() {
            Reserva r = new Reserva(usuario, aula, base, base.plusHours(2));
            when(reservaRepository.findByAulaId(1L)).thenReturn(List.of(r));

            List<Reserva> resultado = reservaService.getReservasPorAula(1L);

            assertEquals(1, resultado.size());
        }

        @Test
        @DisplayName("Devuelve lista vacía cuando no hay reservas para ese aula")
        void testDevuelveVaciaSinReservas() {
            when(reservaRepository.findByAulaId(1L)).thenReturn(Collections.emptyList());

            List<Reserva> resultado = reservaService.getReservasPorAula(1L);

            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Llama a findByAulaId con el ID correcto")
        void testLlamaRepositorioConIdCorrecto() {
            when(reservaRepository.findByAulaId(42L)).thenReturn(Collections.emptyList());

            reservaService.getReservasPorAula(42L);

            verify(reservaRepository).findByAulaId(42L);
        }

        @Test
        @DisplayName("Llama a findByAulaId exactamente una vez")
        void testLlamaRepositorioUnaVez() {
            when(reservaRepository.findByAulaId(1L)).thenReturn(Collections.emptyList());

            reservaService.getReservasPorAula(1L);

            verify(reservaRepository, times(1)).findByAulaId(1L);
        }

        @Test
        @DisplayName("Devuelve múltiples reservas correctamente")
        void testDevuelveMultiplesReservas() {
            Reserva r1 = new Reserva(usuario, aula, base, base.plusHours(1));
            Reserva r2 = new Reserva(usuario, aula, base.plusHours(2), base.plusHours(3));
            when(reservaRepository.findByAulaId(1L)).thenReturn(Arrays.asList(r1, r2));

            List<Reserva> resultado = reservaService.getReservasPorAula(1L);

            assertEquals(2, resultado.size());
        }
    }

    // ─── realizarReserva ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("realizarReserva")
    class RealizarReservaTests {

        @Test
        @DisplayName("Sin reservas existentes: crea la reserva y la devuelve")
        void testReservaExitosaSinConflictos() {
            Reserva nueva = new Reserva(usuario, aula, base, base.plusHours(2));
            when(reservaRepository.findByAulaId(aula.getId())).thenReturn(Collections.emptyList());
            when(reservaRepository.save(nueva)).thenReturn(nueva);

            Optional<Reserva> resultado = reservaService.realizarReserva(nueva);

            assertTrue(resultado.isPresent());
            assertSame(nueva, resultado.get());
        }

        @Test
        @DisplayName("Sin conflictos: llama a save exactamente una vez")
        void testReservaExitosaLlamaSave() {
            Reserva nueva = new Reserva(usuario, aula, base, base.plusHours(2));
            when(reservaRepository.findByAulaId(aula.getId())).thenReturn(Collections.emptyList());
            when(reservaRepository.save(nueva)).thenReturn(nueva);

            reservaService.realizarReserva(nueva);

            verify(reservaRepository, times(1)).save(nueva);
        }

        @Test
        @DisplayName("Solapamiento total: devuelve Optional vacío")
        void testReservaConflictoSolapamientoTotal() {
            Reserva existente = new Reserva(usuario, aula, base, base.plusHours(4));
            Reserva nueva     = new Reserva(usuario, aula, base.plusHours(1), base.plusHours(3));
            when(reservaRepository.findByAulaId(aula.getId())).thenReturn(List.of(existente));

            Optional<Reserva> resultado = reservaService.realizarReserva(nueva);

            assertFalse(resultado.isPresent());
        }

        @Test
        @DisplayName("Solapamiento al inicio (nueva empieza antes): devuelve Optional vacío")
        void testReservaConflictoSolapamientoAlInicio() {
            // existente: 12:00-14:00  nueva: 11:00-13:00 → solapa
            LocalDateTime ini = base.withHour(12);
            Reserva existente = new Reserva(usuario, aula, ini, ini.plusHours(2));
            Reserva nueva     = new Reserva(usuario, aula, ini.minusHours(1), ini.plusHours(1));
            when(reservaRepository.findByAulaId(aula.getId())).thenReturn(List.of(existente));

            Optional<Reserva> resultado = reservaService.realizarReserva(nueva);

            assertFalse(resultado.isPresent());
        }

        @Test
        @DisplayName("Solapamiento al final (nueva termina después): devuelve Optional vacío")
        void testReservaConflictoSolapamientoAlFinal() {
            // existente: 10:00-12:00  nueva: 11:00-13:00 → solapa
            LocalDateTime ini = base.withHour(10);
            Reserva existente = new Reserva(usuario, aula, ini, ini.plusHours(2));
            Reserva nueva     = new Reserva(usuario, aula, ini.plusHours(1), ini.plusHours(3));
            when(reservaRepository.findByAulaId(aula.getId())).thenReturn(List.of(existente));

            Optional<Reserva> resultado = reservaService.realizarReserva(nueva);

            assertFalse(resultado.isPresent());
        }

        @Test
        @DisplayName("Reserva exactamente contigua (nueva empieza cuando termina la existente): sin conflicto")
        void testReservaSinSolapamientoContigua() {
            // existente: 10:00-12:00  nueva: 12:00-14:00 → sin solapamiento
            LocalDateTime ini = base.withHour(10);
            Reserva existente = new Reserva(usuario, aula, ini, ini.plusHours(2));
            Reserva nueva     = new Reserva(usuario, aula, ini.plusHours(2), ini.plusHours(4));
            when(reservaRepository.findByAulaId(aula.getId())).thenReturn(List.of(existente));
            when(reservaRepository.save(nueva)).thenReturn(nueva);

            Optional<Reserva> resultado = reservaService.realizarReserva(nueva);

            assertTrue(resultado.isPresent());
        }

        @Test
        @DisplayName("Reserva antes de la existente (sin solapamiento): se crea correctamente")
        void testReservaSinSolapamientoAntes() {
            // existente: 14:00-16:00  nueva: 10:00-12:00 → sin solapamiento
            LocalDateTime ini = base.withHour(14);
            Reserva existente = new Reserva(usuario, aula, ini, ini.plusHours(2));
            Reserva nueva     = new Reserva(usuario, aula, ini.minusHours(4), ini.minusHours(2));
            when(reservaRepository.findByAulaId(aula.getId())).thenReturn(List.of(existente));
            when(reservaRepository.save(nueva)).thenReturn(nueva);

            Optional<Reserva> resultado = reservaService.realizarReserva(nueva);

            assertTrue(resultado.isPresent());
        }

        @Test
        @DisplayName("Conflicto: no llama a save")
        void testReservaConflictoNoLlamaSave() {
            Reserva existente = new Reserva(usuario, aula, base, base.plusHours(2));
            Reserva nueva     = new Reserva(usuario, aula, base.plusHours(1), base.plusHours(3));
            when(reservaRepository.findByAulaId(aula.getId())).thenReturn(List.of(existente));

            reservaService.realizarReserva(nueva);

            verify(reservaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Múltiples reservas existentes, sólo una solapa: devuelve Optional vacío")
        void testReservaConflictoConVariasExistentes() {
            Reserva e1 = new Reserva(usuario, aula, base.withHour(8),  base.withHour(9));
            Reserva e2 = new Reserva(usuario, aula, base.withHour(10), base.withHour(12)); // este solapa
            Reserva nueva = new Reserva(usuario, aula, base.withHour(11), base.withHour(13));
            when(reservaRepository.findByAulaId(aula.getId())).thenReturn(Arrays.asList(e1, e2));

            Optional<Reserva> resultado = reservaService.realizarReserva(nueva);

            assertFalse(resultado.isPresent());
        }

        @Test
        @DisplayName("Nueva reserva cubre exactamente el mismo bloque que una existente: devuelve Optional vacío")
        void testReservaIdenticoBloque() {
            Reserva existente = new Reserva(usuario, aula, base, base.plusHours(2));
            Reserva nueva     = new Reserva(usuario, aula, base, base.plusHours(2));
            when(reservaRepository.findByAulaId(aula.getId())).thenReturn(List.of(existente));

            Optional<Reserva> resultado = reservaService.realizarReserva(nueva);

            assertFalse(resultado.isPresent());
        }

        @Test
        @DisplayName("Nueva reserva engloba por completo una existente: devuelve Optional vacío")
        void testReservaNuevaEnglobaExistente() {
            // existente: 11:00-12:00  nueva: 10:00-13:00 → solapa
            Reserva existente = new Reserva(usuario, aula, base.plusHours(1), base.plusHours(2));
            Reserva nueva     = new Reserva(usuario, aula, base, base.plusHours(3));
            when(reservaRepository.findByAulaId(aula.getId())).thenReturn(List.of(existente));

            Optional<Reserva> resultado = reservaService.realizarReserva(nueva);

            assertFalse(resultado.isPresent());
        }

        @Test
        @DisplayName("El objeto guardado tiene los datos de la reserva nueva (captura con ArgumentCaptor)")
        void testReservaDatosGuardadosCorrectos() {
            Reserva nueva = new Reserva(usuario, aula, base, base.plusHours(2));
            when(reservaRepository.findByAulaId(aula.getId())).thenReturn(Collections.emptyList());
            ArgumentCaptor<Reserva> captor = ArgumentCaptor.forClass(Reserva.class);
            when(reservaRepository.save(captor.capture())).thenReturn(nueva);

            reservaService.realizarReserva(nueva);

            Reserva captured = captor.getValue();
            assertEquals(base, captured.getFechaHoraInicio());
            assertEquals(base.plusHours(2), captured.getFechaHoraFin());
        }
    }

    // ─── obtenerReservasActivas ───────────────────────────────────────────────────

    @Nested
    @DisplayName("obtenerReservasActivas")
    class ObtenerReservasActivasTests {

        @Test
        @DisplayName("Devuelve la lista del repositorio sin modificar")
        void testDevuelveLista() {
            Reserva r = new Reserva(usuario, aula, base, base.plusHours(1));
            when(reservaRepository.findReservasActivas()).thenReturn(List.of(r));

            List<Reserva> resultado = reservaService.obtenerReservasActivas();

            assertEquals(1, resultado.size());
        }

        @Test
        @DisplayName("Devuelve lista vacía cuando no hay reservas activas")
        void testDevuelveVaciaSinActivas() {
            when(reservaRepository.findReservasActivas()).thenReturn(Collections.emptyList());

            List<Reserva> resultado = reservaService.obtenerReservasActivas();

            assertTrue(resultado.isEmpty());
        }

        @Test
        @DisplayName("Llama a findReservasActivas exactamente una vez")
        void testLlamaRepositorioUnaVez() {
            when(reservaRepository.findReservasActivas()).thenReturn(Collections.emptyList());

            reservaService.obtenerReservasActivas();

            verify(reservaRepository, times(1)).findReservasActivas();
        }

        @Test
        @DisplayName("Devuelve múltiples reservas activas correctamente")
        void testDevuelveMultiplesActivas() {
            Reserva r1 = new Reserva(usuario, aula, base, base.plusHours(1));
            Reserva r2 = new Reserva(usuario, aula, base.plusHours(2), base.plusHours(3));
            Reserva r3 = new Reserva(usuario, aula, base.plusHours(4), base.plusHours(5));
            when(reservaRepository.findReservasActivas()).thenReturn(Arrays.asList(r1, r2, r3));

            List<Reserva> resultado = reservaService.obtenerReservasActivas();

            assertEquals(3, resultado.size());
        }

        @Test
        @DisplayName("No llama a findByAulaId sino a findReservasActivas")
        void testLlamaMetodoCorrecto() {
            when(reservaRepository.findReservasActivas()).thenReturn(Collections.emptyList());

            reservaService.obtenerReservasActivas();

            verify(reservaRepository, never()).findByAulaId(anyLong());
            verify(reservaRepository).findReservasActivas();
        }
    }
}