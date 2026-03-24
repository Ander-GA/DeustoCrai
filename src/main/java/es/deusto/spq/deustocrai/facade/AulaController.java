package es.deusto.spq.deustocrai.facade;

import es.deusto.spq.deustocrai.entity.Aula;
import es.deusto.spq.deustocrai.entity.Reserva;
import es.deusto.spq.deustocrai.service.ReservaService;
import es.deusto.spq.deustocrai.dao.AulaRepository;
import es.deusto.spq.deustocrai.dao.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/salas")
public class AulaController {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private AulaRepository aulaRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @GetMapping
    public List<Aula> listarSalas() {
        return aulaRepository.findAll();
    }

    @PostMapping("/reservar")
    public ResponseEntity<?> reservarSala(@RequestBody Reserva reserva) {
        // Se utiliza el servicio para validar y persistir en la base de datos
        return reservaService.realizarReserva(reserva)
            .<ResponseEntity<?>>map(nueva -> new ResponseEntity<>(nueva, HttpStatus.OK)) 
            .orElseGet(() -> new ResponseEntity<>(
                "La sala ya está reservada en esta franja horaria.", 
                HttpStatus.CONFLICT
            ));
    }

    @GetMapping("/{id}/reservas")
    public List<Reserva> consultarReservas(@PathVariable Long id) {
        return reservaRepository.findByAulaId(id);
    }
}