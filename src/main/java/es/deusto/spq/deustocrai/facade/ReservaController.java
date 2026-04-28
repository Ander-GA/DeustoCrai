package es.deusto.spq.deustocrai.facade;

import es.deusto.spq.deustocrai.entity.Reserva;
import es.deusto.spq.deustocrai.entity.User;
import es.deusto.spq.deustocrai.service.AuthService;
import es.deusto.spq.deustocrai.service.ReservaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {
	
	

    private final ReservaService reservaService;

    @Autowired
    private AuthService authService;
    
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
    
    @GetMapping("/activas")
    public ResponseEntity<List<Reserva>> obtenerReservasActivas() {
        // Al ser salas ocupadas, podría ser un dato público o requerir token según tus reglas de negocio
        return ResponseEntity.ok(reservaService.obtenerReservasActivas());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelarReserva(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        User user = authService.getEmpleadoByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sesión inválida.");

        boolean cancelada = reservaService.cancelarReserva(id, user.getId());
        
        if (cancelada) {
            return ResponseEntity.ok().body("Reserva cancelada con éxito");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No se pudo cancelar. Puede que la reserva no exista o no te pertenezca.");
        }
    }

    @PutMapping("/{id}/extender")
    public ResponseEntity<?> extenderReserva(
            @PathVariable Long id, 
            @RequestParam int minutos, 
            @RequestHeader("Authorization") String token) {
            
        User user = authService.getEmpleadoByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sesión inválida.");

        Optional<Reserva> actualizada = reservaService.extenderReserva(id, minutos, user.getId());
        
        if (actualizada.isPresent()) {
            return ResponseEntity.ok(actualizada.get());
        } else {
            // Mandamos un mensaje claro al frontend
            return ResponseEntity.status(HttpStatus.CONFLICT).body("No se puede extender: la sala está ocupada en ese horario o hay un error de datos.");
        }
    }
}