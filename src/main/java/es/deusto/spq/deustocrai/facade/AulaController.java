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
import java.util.Optional;
import java.util.stream.Collectors;
import es.deusto.spq.deustocrai.entity.User;
import es.deusto.spq.deustocrai.service.AuthService;
@RestController
@RequestMapping("/api/salas")
public class AulaController {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private AulaRepository aulaRepository;

    @Autowired
    private ReservaRepository reservaRepository;
    
    @Autowired
    private AuthService authService;
    
    @GetMapping
    public List<Aula> listarSalas() {
        return aulaRepository.findAll();
    }

    @PostMapping("/reservar")
    public ResponseEntity<?> reservarSala(@RequestBody Reserva reserva) {
        Optional<Reserva> nuevaReserva = reservaService.realizarReserva(reserva);
        
        if (nuevaReserva.isPresent()) {
            return ResponseEntity.ok(nuevaReserva.get());
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("La sala ya está reservada en esta franja horaria.");
        }
    }

    // Endpoint para obtener las reservas del usuario logueado (Punto 1)
    @GetMapping("/usuario/{usuarioId}")
    public List<Reserva> listarReservasUsuario(@PathVariable Long usuarioId) {
        return reservaRepository.findAll().stream()
                .filter(r -> r.getUsuario() != null && r.getUsuario().getId().equals(usuarioId))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}/reservas")
    public List<Reserva> consultarReservas(@PathVariable Long id) {
        return reservaRepository.findByAulaId(id);
    }
    
    @GetMapping("/mis-reservas")
    public ResponseEntity<List<Reserva>> misReservas(@RequestHeader("Authorization") String token) {
        User user = authService.getEmpleadoByToken(token);
        
        // Si el token no es válido o ha expirado, devolvemos un 401
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // Buscamos las reservas que le pertenecen
        List<Reserva> misReservas = reservaRepository.findAll().stream()
                .filter(r -> r.getUsuario() != null && r.getUsuario().getId().equals(user.getId()))
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(misReservas);
    }

}