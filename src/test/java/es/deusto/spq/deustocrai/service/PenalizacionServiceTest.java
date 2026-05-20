package es.deusto.spq.deustocrai.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.deusto.spq.deustocrai.dao.UserRepository;
import es.deusto.spq.deustocrai.entity.Aviso;
import es.deusto.spq.deustocrai.entity.User;

@Tag("Unitario")
@ExtendWith(MockitoExtension.class)
public class PenalizacionServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificacionService notificacionService;

    @Mock
    private AvisoService avisoService;

    @InjectMocks
    private PenalizacionService penalizacionService;

    private User usuarioBase;

    @BeforeEach
    void setUp() {
        usuarioBase = new User("Ana", "García", "pass123", "ana@deusto.es", User.Role.ESTUDIANTE);
        usuarioBase.setId(1L);
        usuarioBase.setBloqueado(false);
    }

    // ─── aplicarPenalizacion ────────────────────────────────────────────────────

    @Test
    @DisplayName("aplicarPenalizacion: debe bloquear al usuario y fijar la fecha de fin")
    void testAplicarPenalizacion_bloqueaUsuario() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioBase));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User resultado = penalizacionService.aplicarPenalizacion(1L, 7);

        assertTrue(resultado.isBloqueado(), "El usuario debe quedar bloqueado");
        assertNotNull(resultado.getFechaFinPenalizacion(), "Debe tener fecha de fin de penalización");
        assertTrue(
            resultado.getFechaFinPenalizacion().isAfter(LocalDateTime.now().plusDays(6)),
            "La fecha de fin debe ser aproximadamente 7 días desde ahora"
        );
    }

    @Test
    @DisplayName("aplicarPenalizacion: debe llamar a notificacionService.notificarPenalizacion")
    void testAplicarPenalizacion_llamaNotificacion() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioBase));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        penalizacionService.aplicarPenalizacion(1L, 3);

        verify(notificacionService, times(1)).notificarPenalizacion(any(User.class), eq(3));
    }

    @Test
    @DisplayName("aplicarPenalizacion: debe crear un aviso de tipo PENALIZACION")
    void testAplicarPenalizacion_creaAviso() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioBase));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        penalizacionService.aplicarPenalizacion(1L, 5);

        verify(avisoService, times(1)).crearAviso(
            any(User.class),
            eq(Aviso.TipoAviso.PENALIZACION),
            eq("Penalización aplicada"),
            any(String.class)
        );
    }

    @Test
    @DisplayName("aplicarPenalizacion: el mensaje del aviso debe mencionar los días de penalización")
    void testAplicarPenalizacion_mensajeAvisoContieneDias() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioBase));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<String> mensajeCaptor = ArgumentCaptor.forClass(String.class);

        penalizacionService.aplicarPenalizacion(1L, 10);

        verify(avisoService).crearAviso(
            any(User.class),
            eq(Aviso.TipoAviso.PENALIZACION),
            any(String.class),
            mensajeCaptor.capture()
        );

        assertTrue(
            mensajeCaptor.getValue().contains("10"),
            "El mensaje del aviso debe indicar el número de días"
        );
    }

    @Test
    @DisplayName("aplicarPenalizacion: debe guardar el usuario con userRepository.save")
    void testAplicarPenalizacion_guardaUsuario() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioBase));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        penalizacionService.aplicarPenalizacion(1L, 7);

        verify(userRepository, times(1)).save(usuarioBase);
    }

    @Test
    @DisplayName("aplicarPenalizacion: debe lanzar IllegalArgumentException si el usuario no existe")
    void testAplicarPenalizacion_usuarioNoEncontrado() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
            IllegalArgumentException.class,
            () -> penalizacionService.aplicarPenalizacion(99L, 7),
            "Debe lanzar excepción cuando el usuario no existe"
        );

        verify(userRepository, never()).save(any());
        verify(notificacionService, never()).notificarPenalizacion(any(), anyInt());
        verify(avisoService, never()).crearAviso(any(), any(), any(), any());
    }

    @Test
    @DisplayName("aplicarPenalizacion: penalización de 1 día fija la fecha de fin correctamente")
    void testAplicarPenalizacion_unDia() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioBase));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User resultado = penalizacionService.aplicarPenalizacion(1L, 1);

        assertNotNull(resultado.getFechaFinPenalizacion());
        assertTrue(resultado.getFechaFinPenalizacion().isAfter(LocalDateTime.now()));
        assertTrue(resultado.getFechaFinPenalizacion().isBefore(LocalDateTime.now().plusDays(2)));
    }

    // ─── eliminarPenalizacion ───────────────────────────────────────────────────

    @Test
    @DisplayName("eliminarPenalizacion: debe desbloquear al usuario y borrar la fecha de fin")
    void testEliminarPenalizacion_desbloqueaUsuario() {
        usuarioBase.setBloqueado(true);
        usuarioBase.setFechaFinPenalizacion(LocalDateTime.now().plusDays(3));

        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioBase));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User resultado = penalizacionService.eliminarPenalizacion(1L);

        assertFalse(resultado.isBloqueado(), "El usuario debe quedar desbloqueado");
        assertNull(resultado.getFechaFinPenalizacion(), "La fecha de fin debe ser null");
    }

    @Test
    @DisplayName("eliminarPenalizacion: debe guardar el usuario actualizado")
    void testEliminarPenalizacion_guardaUsuario() {
        usuarioBase.setBloqueado(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioBase));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        penalizacionService.eliminarPenalizacion(1L);

        verify(userRepository, times(1)).save(usuarioBase);
    }

    @Test
    @DisplayName("eliminarPenalizacion: debe lanzar IllegalArgumentException si el usuario no existe")
    void testEliminarPenalizacion_usuarioNoEncontrado() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
            IllegalArgumentException.class,
            () -> penalizacionService.eliminarPenalizacion(99L)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("eliminarPenalizacion: funciona aunque el usuario ya estuviera desbloqueado")
    void testEliminarPenalizacion_usuarioYaDesbloqueado() {
        // El usuario ya está desbloqueado (caso idempotente)
        usuarioBase.setBloqueado(false);
        usuarioBase.setFechaFinPenalizacion(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioBase));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User resultado = penalizacionService.eliminarPenalizacion(1L);

        assertFalse(resultado.isBloqueado());
        assertNull(resultado.getFechaFinPenalizacion());
        verify(userRepository, times(1)).save(any());
    }

    // ─── obtenerUsuariosPenalizados ─────────────────────────────────────────────

    @Test
    @DisplayName("obtenerUsuariosPenalizados: devuelve solo los usuarios bloqueados")
    void testObtenerUsuariosPenalizados_filtraCorrectamente() {
        User u1 = new User("Luis", "P", "p", "luis@deusto.es", User.Role.ESTUDIANTE);
        u1.setBloqueado(true);

        User u2 = new User("Marta", "Q", "p", "marta@deusto.es", User.Role.ESTUDIANTE);
        u2.setBloqueado(false);

        User u3 = new User("Pedro", "R", "p", "pedro@deusto.es", User.Role.ESTUDIANTE);
        u3.setBloqueado(true);

        when(userRepository.findAll()).thenReturn(Arrays.asList(u1, u2, u3));

        List<User> penalizados = penalizacionService.obtenerUsuariosPenalizados();

        assertEquals(2, penalizados.size(), "Deben aparecer exactamente 2 usuarios penalizados");
        assertTrue(penalizados.stream().allMatch(User::isBloqueado),
            "Todos los usuarios de la lista deben estar bloqueados");
    }

    @Test
    @DisplayName("obtenerUsuariosPenalizados: devuelve lista vacía si nadie está bloqueado")
    void testObtenerUsuariosPenalizados_sinPenalizados() {
        User u1 = new User("Luis", "P", "p", "luis@deusto.es", User.Role.ESTUDIANTE);
        u1.setBloqueado(false);

        when(userRepository.findAll()).thenReturn(Collections.singletonList(u1));

        List<User> penalizados = penalizacionService.obtenerUsuariosPenalizados();

        assertTrue(penalizados.isEmpty(), "La lista debe estar vacía si no hay penalizados");
    }

    @Test
    @DisplayName("obtenerUsuariosPenalizados: devuelve lista vacía si no hay usuarios en el sistema")
    void testObtenerUsuariosPenalizados_sinUsuarios() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<User> penalizados = penalizacionService.obtenerUsuariosPenalizados();

        assertTrue(penalizados.isEmpty());
    }

    @Test
    @DisplayName("obtenerUsuariosPenalizados: devuelve todos los usuarios si todos están bloqueados")
    void testObtenerUsuariosPenalizados_todosBlockeados() {
        User u1 = new User("A", "A", "p", "a@deusto.es", User.Role.ESTUDIANTE);
        u1.setBloqueado(true);
        User u2 = new User("B", "B", "p", "b@deusto.es", User.Role.ESTUDIANTE);
        u2.setBloqueado(true);

        when(userRepository.findAll()).thenReturn(Arrays.asList(u1, u2));

        List<User> penalizados = penalizacionService.obtenerUsuariosPenalizados();

        assertEquals(2, penalizados.size());
    }
}