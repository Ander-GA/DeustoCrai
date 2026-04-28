package es.deusto.spq.deustocrai.facade;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.deusto.spq.deustocrai.entity.Reserva;
import es.deusto.spq.deustocrai.entity.Aula;
import es.deusto.spq.deustocrai.entity.User;
import es.deusto.spq.deustocrai.dao.AulaRepository;
import es.deusto.spq.deustocrai.dao.UserRepository;
import org.junit.jupiter.api.Tag;

@Tag("Unitario")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional 
public class ReservaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AulaRepository aulaRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Debería crear una reserva correctamente")
    public void testCrearReservaSuccess() throws Exception {
        // 1. Obtener datos reales de la BD
        Aula aula = aulaRepository.findAll().get(0);
        User user = userRepository.findAll().get(0); // Obtenemos el primer usuario existente

        // 2. Definir una franja horaria (Limpiamos segundos y nanos para evitar problemas en el JSON)
        LocalDateTime inicio = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime fin = inicio.plusHours(3);

        Reserva nueva = new Reserva(user, aula, inicio, fin);

        // 3. Petición corregida al endpoint actual
        mockMvc.perform(post("/api/salas/reservar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nueva)))
                .andExpect(status().isOk()) // Esperamos 200 OK
                .andExpect(jsonPath("$.aula.id", is(aula.getId().intValue())));
    }

    @Test
    @DisplayName("Debería fallar si la reserva se solapa con una existente")
    public void testCrearReservaConflict() throws Exception {
        Aula aula = aulaRepository.findAll().get(0);
        User user = userRepository.findAll().get(0);
        
        LocalDateTime inicio = LocalDateTime.now().plusDays(2).withHour(15).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime fin = inicio.plusHours(3);

        Reserva reserva1 = new Reserva(user, aula, inicio, fin);
        
        // Guardamos la primera reserva con éxito
        mockMvc.perform(post("/api/salas/reservar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reserva1)))
                .andExpect(status().isOk()); // Esperamos 200 OK

        // Intentamos reservar la misma sala en el mismo horario
        mockMvc.perform(post("/api/salas/reservar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reserva1)))
                .andExpect(status().isConflict()); // Esperamos 409 Conflict
    }
}