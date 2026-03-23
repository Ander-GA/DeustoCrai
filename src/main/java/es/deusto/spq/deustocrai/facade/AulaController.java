package es.deusto.spq.deustocrai.facade;

import es.deusto.spq.deustocrai.dao.AulaRepository;
import es.deusto.spq.deustocrai.dao.ReservaRepository;
import es.deusto.spq.deustocrai.entity.Aula;
import es.deusto.spq.deustocrai.entity.Reserva;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador para la gestión de salas de estudio y reservas.
 * Implementa la lógica de disponibilidad por franjas horarias (3 horas).
 */
@RestController
@RequestMapping("/api/salas")
public class AulaController {

    @Autowired
    private AulaRepository aulaRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    // Obtener todas las salas disponibles en el sistema
    @GetMapping
    public List<Aula> listarSalas() {
        return aulaRepository.findAll();
    }

    /**
     * Consulta la disponibilidad de una sala para una fecha específica.
     * Filtra las reservas existentes para ayudar a renderizar el calendario visual.
     */
    @GetMapping("/{id}/disponibilidad")
    public List<Reserva> consultarDisponibilidadDia(@PathVariable Long id, @RequestParam String fecha) {
        LocalDate date = LocalDate.parse(fecha);
        // Retorna las reservas de esa sala que coinciden con el día solicitado
        return reservaRepository.findByAulaId(id).stream()
                .filter(r -> r.getFechaHoraInicio().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    /**
     * Realiza una reserva validando que la franja horaria no esté ocupada.
     * Las franjas permitidas son de 3 horas (Ej: 08:30 - 11:30).
     */
    @PostMapping("/reservar")
    public ResponseEntity<?> reservarSala(@RequestBody Reserva reserva) {
        // 1. Obtener reservas actuales de la sala
        List<Reserva> existentes = reservaRepository.findByAulaId(reserva.getAula().getId());
        
        // 2. Validar si ya existe una reserva que empiece exactamente a la misma hora
        // Esto previene solapamientos en el sistema de franjas fijas
        boolean ocupada = existentes.stream().anyMatch(r -> 
            r.getFechaHoraInicio().equals(reserva.getFechaHoraInicio())
        );

        if (ocupada) {
            // Retorna un error 409 Conflict si la sala ya no está disponible
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("La sala ya está reservada en esta franja horaria.");
        }

        // 3. Guardar la nueva reserva en la base de datos
        Reserva nuevaReserva = reservaRepository.save(reserva);
        return ResponseEntity.ok(nuevaReserva);
    }

    // Endpoint adicional para consultar las reservas generales de una sala
    @GetMapping("/{id}/reservas")
    public List<Reserva> consultarReservas(@PathVariable Long id) {
        return reservaRepository.findByAulaId(id);
    }

    // Endpoint para la creación de nuevas salas (Uso administrativo)
    @PostMapping
    public Aula crearSala(@RequestBody Aula aula) {
        return aulaRepository.save(aula);
    }
}