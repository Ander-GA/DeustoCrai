package es.deusto.spq.deustocrai.facade;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Tag;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.deusto.spq.deustocrai.dao.MaterialRepository;
import es.deusto.spq.deustocrai.entity.Material;

@Tag("Unitario")
@WebMvcTest(MaterialController.class)
public class MaterialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // A diferencia de los libros, este controlador ataca directo al repositorio
    @MockitoBean
    private MaterialRepository materialRepository;

    private Material materialMock;

    @BeforeEach
    void setUp() {
        materialMock = new Material();
        // Inyectamos el ID por reflection por si la entidad no tiene setId
        org.springframework.test.util.ReflectionTestUtils.setField(materialMock, "id", 1L);
        materialMock.setTitulo("Portátil Dell XPS");
    }

    @Test
    @DisplayName("GET /api/materiales - Retorna la lista de todos los materiales")
    public void testListarMateriales() throws Exception {
        when(materialRepository.findAll()).thenReturn(Arrays.asList(materialMock));

        mockMvc.perform(get("/api/materiales"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/materiales?buscar=... - Retorna materiales filtrados")
    public void testBuscarMateriales() throws Exception {
        when(materialRepository.findByTituloContainingIgnoreCase("Portátil")).thenReturn(Arrays.asList(materialMock));

        mockMvc.perform(get("/api/materiales")
                .param("buscar", "Portátil"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/materiales/{id} - Retorna 200 y el material si existe")
    public void testObtenerDetallesMaterialExiste() throws Exception {
        when(materialRepository.findById(1L)).thenReturn(Optional.of(materialMock));

        mockMvc.perform(get("/api/materiales/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/materiales/{id} - Retorna 404 si el material no existe")
    public void testObtenerDetallesMaterialNoExiste() throws Exception {
        when(materialRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/materiales/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/materiales - Crea un nuevo material y retorna 201 Created")
    public void testCrearMaterial() throws Exception {
        when(materialRepository.save(any(Material.class))).thenReturn(materialMock);

        mockMvc.perform(post("/api/materiales")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(materialMock)))
                .andExpect(status().isCreated());
    }
}