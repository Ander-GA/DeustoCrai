package es.deusto.spq.deustocrai.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
import es.deusto.spq.deustocrai.dao.PrestamoRepository;
import es.deusto.spq.deustocrai.dao.ValoracionRepository;
import es.deusto.spq.deustocrai.entity.Libro;
import es.deusto.spq.deustocrai.entity.Prestamo;
import es.deusto.spq.deustocrai.entity.User;
import es.deusto.spq.deustocrai.entity.Valoracion;

@Tag("Unitario")
@ExtendWith(MockitoExtension.class)
public class ValoracionServiceTest {

    @Mock private ValoracionRepository valoracionRepository;
    @Mock private PrestamoRepository prestamoRepository;
    @Mock private AbstractRecursoRepository recursoRepository;

    @InjectMocks
    private ValoracionService valoracionService;

    private User usuario;
    private Libro recurso;
    private Prestamo prestamo;

    @BeforeEach
    void setUp() {
        usuario = new User();
        usuario.setId(1L);

        recurso = new Libro();
        recurso.setId(100L);
        recurso.setTitulo("Clean Architecture");

        prestamo = new Prestamo(usuario, recurso);
        prestamo.setEstado(Prestamo.EstadoPrestamo.DEVUELTO);
    }

    @Test
    @DisplayName("Dejar reseña - Éxito")
    void testDejarResenaExito() {
        when(recursoRepository.findById(100L)).thenReturn(Optional.of(recurso));
        when(prestamoRepository.findByUsuarioIdAndEstado(1L, Prestamo.EstadoPrestamo.DEVUELTO))
                .thenReturn(Arrays.asList(prestamo));
        when(valoracionRepository.existsByUsuarioIdAndRecursoId(1L, 100L)).thenReturn(false);
        
        Valoracion expected = new Valoracion(usuario, recurso, 5, "Excelente");
        when(valoracionRepository.save(any(Valoracion.class))).thenReturn(expected);

        Valoracion result = valoracionService.dejarResena(usuario, 100L, 5, "Excelente");

        assertNotNull(result);
        assertEquals(5, result.getPuntuacion());
    }

    @Test
    @DisplayName("Dejar reseña - Excepción por puntuación inválida")
    void testDejarResenaPuntuacionInvalida() {
        assertThrows(IllegalArgumentException.class, () -> {
            valoracionService.dejarResena(usuario, 100L, 6, "Exagerado");
        });
    }

    @Test
    @DisplayName("Dejar reseña - Excepción si el recurso no existe")
    void testDejarResenaRecursoNoEncontrado() {
        when(recursoRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            valoracionService.dejarResena(usuario, 999L, 5, "Genial");
        });
    }

    @Test
    @DisplayName("Dejar reseña - Excepción si no se ha alquilado/devuelto")
    void testDejarResenaSinDevolver() {
        when(recursoRepository.findById(100L)).thenReturn(Optional.of(recurso));
        when(prestamoRepository.findByUsuarioIdAndEstado(1L, Prestamo.EstadoPrestamo.DEVUELTO))
                .thenReturn(Collections.emptyList());

        assertThrows(IllegalStateException.class, () -> {
            valoracionService.dejarResena(usuario, 100L, 4, "Muy bueno");
        });
    }

    @Test
    @DisplayName("Dejar reseña - Excepción si ya ha valorado antes")
    void testDejarResenaYaValorada() {
        when(recursoRepository.findById(100L)).thenReturn(Optional.of(recurso));
        when(prestamoRepository.findByUsuarioIdAndEstado(1L, Prestamo.EstadoPrestamo.DEVUELTO))
                .thenReturn(Arrays.asList(prestamo));
        when(valoracionRepository.existsByUsuarioIdAndRecursoId(1L, 100L)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> {
            valoracionService.dejarResena(usuario, 100L, 4, "Bueno");
        });
    }

    @Test
    @DisplayName("Obtener reseñas de un recurso")
    void testObtenerResenas() {
        Valoracion v1 = new Valoracion(usuario, recurso, 5, "Genial");
        when(valoracionRepository.findByRecursoIdOrderByFechaDesc(100L))
                .thenReturn(Arrays.asList(v1));

        List<Valoracion> resultados = valoracionService.obtenerResenasDeRecurso(100L);

        assertEquals(1, resultados.size());
        assertEquals("Genial", resultados.get(0).getComentario());
    }
}