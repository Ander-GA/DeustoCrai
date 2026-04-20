package es.deusto.spq.deustocrai.facade;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.jayway.jsonpath.JsonPath;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.deusto.spq.deustocrai.entity.Libro;

@SpringBootTest
@AutoConfigureMockMvc
public class LibroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Debería registrar un nuevo libro correctamente y devolver 201 Created")
    public void testAnadirLibroSuccess() throws Exception {
        Libro nuevoLibro = new Libro("El Señor de los Anillos", "978-0261102385", "J.R.R. Tolkien");

        mockMvc.perform(post("/api/libros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoLibro)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo", is("El Señor de los Anillos")))
                .andExpect(jsonPath("$.isbn", is("978-0261102385")))
                .andExpect(jsonPath("$.disponible", is(true)));
    }

    @Test
    @DisplayName("Debería borrar un libro existente y devolver 204 No Content")
    public void testBorrarLibroSuccess() throws Exception {
        Libro libroTemporal = new Libro("Libro Para Borrar", "000-0000000000", "Autor Anonimo");
        
        MvcResult result = mockMvc.perform(post("/api/libros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(libroTemporal)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String responseBody = result.getResponse().getContentAsString();
        Integer id = JsonPath.read(responseBody, "$.id");

        mockMvc.perform(delete("/api/libros/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/libros/" + id))
                .andExpect(status().isNotFound());
    }
}