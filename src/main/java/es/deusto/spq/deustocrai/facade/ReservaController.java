package es.deusto.spq.deustocrai.facade;

import es.deusto.spq.deustocrai.entity.Reserva;
import es.deusto.spq.deustocrai.service.ReservaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping("/aula/{aulaId}")
    public List<Reserva> listarPorAula(@PathVariable Long aulaId) {
        return reservaService.getReservasPorAula(aulaId);
    }
    
    @GetMapping("/eventos/{aulaId}")
    public List<Map<String, Object>> obtenerEventosCalendario(@PathVariable Long aulaId) {
        List<Reserva> reservas = reservaService.getReservasPorAula(aulaId);
        return reservas.stream().map(res -> {
            Map<String, Object> evento = new HashMap<>();
            evento.put("id", res.getId());
            evento.put("title", "Ocupado");
            evento.put("start", res.getFechaHoraInicio());
            evento.put("end", res.getFechaHoraFin());
            evento.put("color", "#d9534f"); // Rojo para ocupado
            return evento;
        }).collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<?> crearReserva(@RequestBody Reserva reserva) {
        return reservaService.realizarReserva(reserva)
            .map(r -> new ResponseEntity<>(r, HttpStatus.CREATED))
            .orElseGet(() -> new ResponseEntity<>(HttpStatus.CONFLICT)); // 409 si está ocupada
    }
}