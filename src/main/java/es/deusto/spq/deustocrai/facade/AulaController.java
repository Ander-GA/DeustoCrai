package es.deusto.spq.deustocrai.facade;

import es.deusto.spq.deustocrai.dao.AulaRepository;

import es.deusto.spq.deustocrai.dao.ReservaRepository;
import es.deusto.spq.deustocrai.entity.Aula;
import es.deusto.spq.deustocrai.entity.Reserva;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/salas")
public class AulaController {

    @Autowired
    private AulaRepository aulaRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    // Tarea #60: Consultar todas las salas y su estado
    @GetMapping
    public List<Aula> listarSalas() {
        return aulaRepository.findAll();
    }

    // Tarea #60: Consultar reservas de una sala específica
    @GetMapping("/{id}/reservas")
    public List<Reserva> consultarReservas(@PathVariable Long id) {
        return reservaRepository.findByAulaId(id);
    }

    // Tarea #59: Crear una nueva sala (para admins)
    @PostMapping
    public Aula crearSala(@RequestBody Aula aula) {
        return aulaRepository.save(aula);
    }

    // Tarea #59: Realizar una reserva
    @PostMapping("/reservar")
    public ResponseEntity<Reserva> reservarSala(@RequestBody Reserva reserva) {
        // Aquí podrías añadir lógica para verificar si el aula está libre en ese horario
        Reserva nuevaReserva = reservaRepository.save(reserva);
        return ResponseEntity.ok(nuevaReserva);
    }
}