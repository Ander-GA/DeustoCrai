package es.deusto.spq.deustocrai.facade;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

import es.deusto.spq.deustocrai.dao.LibroRepository;
import es.deusto.spq.deustocrai.entity.Libro;
import es.deusto.spq.deustocrai.service.LibroService;

@Tag("Unitario")
@WebMvcTest(LibroController.class)
public class LibroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Inyectamos los mocks necesarios para el controlador de Libros
    @MockitoBean
    private LibroService libroService;

    @MockitoBean
    private LibroRepository libroRepository;

    private Libro libroMock;

    @BeforeEach
    void setUp() {
        libroMock = new Libro();
        org.springframework.test.util.ReflectionTestUtils.setField(libroMock, "id", 1L);
        libroMock.setTitulo("El Señor de los Anillos");
        libroMock.setAutor("J.R.R. Tolkien");
    }

    @Test
    @DisplayName("GET /api/libros - Retorna la lista de todos los libros")
    public void testListarLibros() throws Exception {
        when(libroService.listarLibros()).thenReturn(Arrays.asList(libroMock));

        mockMvc.perform(get("/api/libros"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/libros/buscar?q=... - Retorna libros filtrados por título")
    public void testBuscarLibros() throws Exception {
        when(libroRepository.findByTituloContainingIgnoreCase("Señor")).thenReturn(Arrays.asList(libroMock));

        mockMvc.perform(get("/api/libros/buscar")
                .param("q", "Señor"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/libros/{id} - Retorna 200 y el libro si existe")
    public void testObtenerDetallesLibroExiste() throws Exception {
        when(libroService.obtenerLibroPorId(1L)).thenReturn(Optional.of(libroMock));

        mockMvc.perform(get("/api/libros/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/libros/{id} - Retorna 404 si el libro no existe")
    public void testObtenerDetallesLibroNoExiste() throws Exception {
        when(libroService.obtenerLibroPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/libros/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/libros - Crea un nuevo libro y retorna 201 Created")
    public void testAnadirLibro() throws Exception {
        when(libroService.anadirLibro(any(Libro.class))).thenReturn(libroMock);

        mockMvc.perform(post("/api/libros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(libroMock)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("DELETE /api/libros/{id} - Retorna 204 No Content si se borra con éxito")
    public void testBorrarLibroExito() throws Exception {
        when(libroService.borrarLibro(1L)).thenReturn(1);

        mockMvc.perform(delete("/api/libros/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/libros/{id} - Retorna 409 Conflict si el libro está prestado")
    public void testBorrarLibroPrestado() throws Exception {
        when(libroService.borrarLibro(1L)).thenReturn(0);

        mockMvc.perform(delete("/api/libros/1"))
                .andExpect(status().isConflict())
                .andExpect(content().string("No se puede borrar el libro porque actualmente se encuentra prestado."));
    }

    @Test
    @DisplayName("DELETE /api/libros/{id} - Retorna 404 Not Found si el libro a borrar no existe")
    public void testBorrarLibroNoEncontrado() throws Exception {
        when(libroService.borrarLibro(99L)).thenReturn(-1); // Cualquier valor que no sea 1 o 0

        mockMvc.perform(delete("/api/libros/99"))
                .andExpect(status().isNotFound());
    }
}