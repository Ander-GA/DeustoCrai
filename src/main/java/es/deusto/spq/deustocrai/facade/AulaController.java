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
    
    @Autowired
    private es.deusto.spq.deustocrai.dao.UserRepository userRepository;
    
    @GetMapping
    public List<Aula> listarSalas() {
        return aulaRepository.findAll();
    }

    @PostMapping("/reservar")
    public ResponseEntity<?> reservarSala(@RequestBody Reserva reserva) {
        
        // 1. Buscamos al usuario en la base de datos para ver su estado real
        Optional<es.deusto.spq.deustocrai.entity.User> optUser = userRepository.findById(reserva.getUsuario().getId());
        if (optUser.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        
        es.deusto.spq.deustocrai.entity.User user = optUser.get();

        // 2. Control de Penalizaciones (Tickets #23, #80, #24, #82)
        if (user.isBloqueado()) {
            if (user.getFechaFinPenalizacion() != null && java.time.LocalDateTime.now().isAfter(user.getFechaFinPenalizacion())) {
                // Ya ha pasado el tiempo de castigo: Levantamos penalización
                user.setBloqueado(false);
                user.setFechaFinPenalizacion(null);
                userRepository.save(user);
            } else {
                // Sigue castigado: Impedimos la reserva
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Estás penalizado por no devolver una sala a tiempo. Tu penalización termina a las: " + 
                              user.getFechaFinPenalizacion().toLocalTime());
            }
        }

        // 3. Proceso normal si no está bloqueado
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