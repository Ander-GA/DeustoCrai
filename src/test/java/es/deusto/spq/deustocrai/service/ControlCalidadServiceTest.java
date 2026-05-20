package es.deusto.spq.deustocrai.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.deusto.spq.deustocrai.dao.ControlCalidadRepository;
import es.deusto.spq.deustocrai.dao.MaterialRepository;
import es.deusto.spq.deustocrai.dao.PrestamoRepository;
import es.deusto.spq.deustocrai.entity.Aviso;
import es.deusto.spq.deustocrai.entity.ControlCalidad;
import es.deusto.spq.deustocrai.entity.ControlCalidad.EstadoControl;
import es.deusto.spq.deustocrai.entity.Material;
import es.deusto.spq.deustocrai.entity.Prestamo;
import es.deusto.spq.deustocrai.entity.User;

@Tag("Unitario")
@ExtendWith(MockitoExtension.class)
public class ControlCalidadServiceTest {

    @Mock private ControlCalidadRepository controlCalidadRepository;
    @Mock private MaterialRepository materialRepository;
    @Mock private PrestamoRepository prestamoRepository;
    @Mock private AvisoService avisoService;

    @InjectMocks
    private ControlCalidadService controlCalidadService;

    private User bibliotecario;
    private Material material;
    private Prestamo prestamo;

    @BeforeEach
    void setUp() {
        bibliotecario = new User();
        bibliotecario.setId(1L);

        material = new Material();
        // Usamos reflection o setters si lo configuraste anteriormente
        org.springframework.test.util.ReflectionTestUtils.setField(material, "id", 100L);
        material.setTitulo("Portátil Dell");

        prestamo = new Prestamo(new User(), material);
        org.springframework.test.util.ReflectionTestUtils.setField(prestamo, "id", 50L);
    }

    @Test
    @DisplayName("Registrar material APTO (sin préstamo asociado) - Éxito")
    void testRegistrarControlApto() {
        when(materialRepository.findById(100L)).thenReturn(Optional.of(material));
        when(controlCalidadRepository.save(any(ControlCalidad.class)))
            .thenAnswer(i -> i.getArgument(0));

        ControlCalidad result = controlCalidadService.registrarControl(
                100L, null, bibliotecario, EstadoControl.APTO, "");

        assertNotNull(result);
        assertEquals(EstadoControl.APTO, result.getEstado());
        assertEquals("Sin observaciones", result.getObservaciones());
        assertTrue(material.isDisponible());
        verify(materialRepository).save(material);
        verify(avisoService, never()).crearAviso(any(), any(), anyString(), anyString());
    }

    @Test
    @DisplayName("Registrar material ROTO (con préstamo) y genera aviso")
    void testRegistrarControlRoto() {
        when(materialRepository.findById(100L)).thenReturn(Optional.of(material));
        when(prestamoRepository.findById(50L)).thenReturn(Optional.of(prestamo));
        when(controlCalidadRepository.save(any(ControlCalidad.class)))
            .thenAnswer(i -> i.getArgument(0));

        ControlCalidad result = controlCalidadService.registrarControl(
                100L, 50L, bibliotecario, EstadoControl.ROTO, "Pantalla rota");

        assertNotNull(result);
        assertEquals(prestamo, result.getPrestamo());
        assertFalse(material.isDisponible());
        
        verify(avisoService).crearAviso(eq(bibliotecario), eq(Aviso.TipoAviso.RECORDATORIO_DEVOLUCION), 
                anyString(), contains("ROTO"));
    }

    @Test
    @DisplayName("Registrar material REPARACION y genera aviso")
    void testRegistrarControlReparacion() {
        when(materialRepository.findById(100L)).thenReturn(Optional.of(material));
        when(controlCalidadRepository.save(any(ControlCalidad.class)))
            .thenAnswer(i -> i.getArgument(0));

        ControlCalidad result = controlCalidadService.registrarControl(
                100L, null, bibliotecario, EstadoControl.REPARACION, "Falla teclado");

        assertFalse(material.isDisponible());
        verify(avisoService).crearAviso(eq(bibliotecario), eq(Aviso.TipoAviso.RECORDATORIO_DEVOLUCION), 
                anyString(), contains("REPARACIÓN"));
    }

    @Test
    @DisplayName("Lanza excepción si material no existe")
    void testMaterialNoEncontrado() {
        when(materialRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            controlCalidadService.registrarControl(99L, null, bibliotecario, EstadoControl.APTO, "Ok");
        });
    }

    @Test
    @DisplayName("Lanza excepción si préstamo no existe")
    void testPrestamoNoEncontrado() {
        when(materialRepository.findById(100L)).thenReturn(Optional.of(material));
        when(prestamoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            controlCalidadService.registrarControl(100L, 99L, bibliotecario, EstadoControl.APTO, "Ok");
        });
    }
}