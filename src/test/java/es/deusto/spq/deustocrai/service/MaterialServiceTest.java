package es.deusto.spq.deustocrai.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.deusto.spq.deustocrai.dao.MaterialRepository;
import es.deusto.spq.deustocrai.entity.Material;
import org.junit.jupiter.api.Tag;

@Tag("Unitario")
@ExtendWith(MockitoExtension.class)
public class MaterialServiceTest {

    @Mock
    private MaterialRepository materialRepository;

    private Material material;

    @BeforeEach
    void setUp() {
        material = new Material("MacBook Pro", "SN-001", "Portatil");
        material.setDisponible(true);
    }

    // ── guardar ────────────────────────────────────────────────────

    @Test
    @DisplayName("guardar: persiste el material correctamente")
    void testGuardarMaterialLoGuarda() {
        when(materialRepository.save(material)).thenReturn(material);

        Material resultado = materialRepository.save(material);

        assertNotNull(resultado);
        verify(materialRepository, times(1)).save(material);
    }

    @Test
    @DisplayName("guardar: los datos del material se conservan tras guardar")
    void testGuardarMaterialDatosCorrectos() {
        when(materialRepository.save(material)).thenReturn(material);

        Material resultado = materialRepository.save(material);

        assertEquals("MacBook Pro", resultado.getTitulo());
        assertEquals("SN-001", resultado.getNumeroSerie());
        assertEquals("Portatil", resultado.getTipo());
        assertTrue(resultado.isDisponible());
    }

    // ── listar ─────────────────────────────────────────────────────

    @Test
    @DisplayName("listar: devuelve todos los materiales")
    void testListarDevuelveTodos() {
        Material material2 = new Material("Canon EOS", "SN-002", "Camara");
        when(materialRepository.findAll()).thenReturn(Arrays.asList(material, material2));

        List<Material> resultado = materialRepository.findAll();

        assertEquals(2, resultado.size());
        verify(materialRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("listar: devuelve lista vacia si no hay materiales")
    void testListarDevuelveListaVacia() {
        when(materialRepository.findAll()).thenReturn(Collections.emptyList());

        List<Material> resultado = materialRepository.findAll();

        assertTrue(resultado.isEmpty());
    }

    // ── obtenerPorId ───────────────────────────────────────────────

    @Test
    @DisplayName("obtenerPorId: devuelve el material si existe")
    void testObtenerPorIdExistente() {
        when(materialRepository.findById(1L)).thenReturn(Optional.of(material));

        Optional<Material> resultado = materialRepository.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals("MacBook Pro", resultado.get().getTitulo());
    }

    @Test
    @DisplayName("obtenerPorId: devuelve Optional vacio si no existe")
    void testObtenerPorIdNoExistente() {
        when(materialRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Material> resultado = materialRepository.findById(99L);

        assertFalse(resultado.isPresent());
    }

    // ── buscarPorTitulo ────────────────────────────────────────────

    @Test
    @DisplayName("buscarPorTitulo: devuelve materiales que contienen el texto")
    void testBuscarPorTituloDevuelveCoincidencias() {
        when(materialRepository.findByTituloContainingIgnoreCase("mac"))
            .thenReturn(List.of(material));

        List<Material> resultado = materialRepository.findByTituloContainingIgnoreCase("mac");

        assertEquals(1, resultado.size());
        assertEquals("MacBook Pro", resultado.get(0).getTitulo());
    }

    @Test
    @DisplayName("buscarPorTitulo: devuelve lista vacia si no hay coincidencias")
    void testBuscarPorTituloSinCoincidencias() {
        when(materialRepository.findByTituloContainingIgnoreCase("xyz"))
            .thenReturn(Collections.emptyList());

        List<Material> resultado = materialRepository.findByTituloContainingIgnoreCase("xyz");

        assertTrue(resultado.isEmpty());
    }

    // ── cambiarDisponibilidad ──────────────────────────────────────

    @Test
    @DisplayName("cambiarDisponibilidad: marca el material como no disponible")
    void testCambiarDisponibilidadAFalse() {
        material.setDisponible(false);
        when(materialRepository.save(material)).thenReturn(material);

        Material resultado = materialRepository.save(material);

        assertFalse(resultado.isDisponible());
    }

    @Test
    @DisplayName("cambiarDisponibilidad: marca el material como disponible de nuevo")
    void testCambiarDisponibilidadATrue() {
        material.setDisponible(false);
        material.setDisponible(true);
        when(materialRepository.save(material)).thenReturn(material);

        Material resultado = materialRepository.save(material);

        assertTrue(resultado.isDisponible());
    }

    // ── eliminar ───────────────────────────────────────────────────

    @Test
    @DisplayName("eliminar: llama a deleteById con el id correcto")
    void testEliminarMaterial() {
        doNothing().when(materialRepository).deleteById(1L);

        materialRepository.deleteById(1L);

        verify(materialRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("eliminar: existsById devuelve false tras borrar")
    void testEliminarMaterialYaNoExiste() {
        when(materialRepository.existsById(1L)).thenReturn(false);

        boolean existe = materialRepository.existsById(1L);

        assertFalse(existe);
    }
}