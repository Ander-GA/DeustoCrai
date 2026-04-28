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
import org.junit.jupiter.api.Tag;

@Tag("Unitario")
@SpringBootTest
@AutoConfigureMockMvc
public class LibroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private es.deusto.spq.deustocrai.dao.UserRepository userRepository;
    
    @Autowired
    private es.deusto.spq.deustocrai.dao.PrestamoRepository prestamoRepository;

    @Autowired
    private es.deusto.spq.deustocrai.dao.LibroRepository libroRepository;
    
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
    @Test
    @DisplayName("Debería fallar (409 Conflict) al borrar un libro que actualmente está prestado")
    public void testBorrarLibroConflicto() throws Exception {
        // 1. Creamos un usuario de prueba en la BD para poder hacer el préstamo
        es.deusto.spq.deustocrai.entity.User user = new es.deusto.spq.deustocrai.entity.User();
        user.setEmail("estudiante-conflicto@deusto.es");
        user.setPassword("password123");
        user.setNombre("Usuario");
        user.setApellidos("Conflicto");
        user.setRole(es.deusto.spq.deustocrai.entity.User.Role.ESTUDIANTE);
        user = userRepository.save(user);

        // 2. Creamos y guardamos un libro real en la BD
        Libro libroPrestado = new Libro("Libro En Préstamo", "123-4567890123", "Autor Pruebas");
        libroPrestado.setDisponible(false); // Lo marcamos como no disponible
        libroPrestado = libroRepository.save(libroPrestado);

        // 3. Creamos el préstamo activo (PENDIENTE_ENTREGA) asociando el usuario y el libro
        es.deusto.spq.deustocrai.entity.Prestamo prestamo = new es.deusto.spq.deustocrai.entity.Prestamo(user, libroPrestado);
        prestamo.setEstado(es.deusto.spq.deustocrai.entity.Prestamo.EstadoPrestamo.PENDIENTE_ENTREGA);
        prestamoRepository.save(prestamo);

        // 4. Intentamos borrar el libro por la API
        mockMvc.perform(delete("/api/libros/" + libroPrestado.getId()))
                .andExpect(status().isConflict()); // Debe devolver 409
    }
}