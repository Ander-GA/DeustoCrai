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

import com.fasterxml.jackson.databind.ObjectMapper;
import es.deusto.spq.deustocrai.entity.Reserva;
import es.deusto.spq.deustocrai.entity.Aula;
import es.deusto.spq.deustocrai.entity.User;
import es.deusto.spq.deustocrai.dao.AulaRepository;
import es.deusto.spq.deustocrai.dao.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
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
        // 1. Obtener datos reales de la BD (creados en DataInitializer)
        Aula aula = aulaRepository.findAll().get(0);
        User user = userRepository.findByEmail("1").get();

        // 2. Definir una franja horaria (mañana a las 10:00)
        LocalDateTime inicio = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime fin = inicio.plusHours(3);

        Reserva nueva = new Reserva(user, aula, inicio, fin);

        mockMvc.perform(post("/api/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nueva)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.aula.id", is(aula.getId().intValue())));
    }

    @Test
    @DisplayName("Debería fallar si la reserva se solapa con una existente")
    public void testCrearReservaConflict() throws Exception {
        Aula aula = aulaRepository.findAll().get(0);
        User user = userRepository.findByEmail("1").get();
        
        LocalDateTime inicio = LocalDateTime.now().plusDays(2).withHour(15).withMinute(0);
        LocalDateTime fin = inicio.plusHours(3);

        Reserva reserva1 = new Reserva(user, aula, inicio, fin);
        
        // Guardamos la primera reserva
        mockMvc.perform(post("/api/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reserva1)))
                .andExpect(status().isCreated());

        // Intentamos reservar la misma sala en el mismo horario
        mockMvc.perform(post("/api/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reserva1)))
                .andExpect(status().isConflict());
    }
}