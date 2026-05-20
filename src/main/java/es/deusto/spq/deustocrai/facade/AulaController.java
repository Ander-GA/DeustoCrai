package es.deusto.spq.deustocrai.facade;

import es.deusto.spq.deustocrai.entity.Aula;
import es.deusto.spq.deustocrai.entity.Reserva;
import es.deusto.spq.deustocrai.entity.BloqueoSala;
import es.deusto.spq.deustocrai.entity.User;
import es.deusto.spq.deustocrai.service.ReservaService;
import es.deusto.spq.deustocrai.service.AuthService;
import es.deusto.spq.deustocrai.dao.AulaRepository;
import es.deusto.spq.deustocrai.dao.ReservaRepository;
import es.deusto.spq.deustocrai.dao.BloqueoSalaRepository;
import es.deusto.spq.deustocrai.dao.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/salas")
public class AulaController {
    
    @Autowired
    private BloqueoSalaRepository bloqueoSalaRepository;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private AulaRepository aulaRepository;

    @Autowired
    private ReservaRepository reservaRepository;
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping
    public List<Aula> listarSalas() {
        return aulaRepository.findAll();
    }

    @PostMapping("/reservar")
    public ResponseEntity<?> reservarSala(@RequestBody Reserva reserva) {
        
        Optional<User> optUser = userRepository.findById(reserva.getUsuario().getId());
        if (optUser.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        
        User user = optUser.get();

        if (user.isBloqueado()) {
            if (user.getFechaFinPenalizacion() != null && LocalDateTime.now().isAfter(user.getFechaFinPenalizacion())) {
                user.setBloqueado(false);
                user.setFechaFinPenalizacion(null);
                userRepository.save(user);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Estás penalizado por no devolver una sala a tiempo. Tu penalización termina a las: " + 
                              user.getFechaFinPenalizacion().toLocalTime());
            }
        }

        Optional<Reserva> nuevaReserva = reservaService.realizarReserva(reserva);
        
        if (nuevaReserva.isPresent()) {
            return ResponseEntity.ok(nuevaReserva.get());
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("La sala ya está reservada en esta franja horaria.");
        }
    }	

    @GetMapping("/usuario/{usuarioId}")
    public List<Reserva> listarReservasUsuario(
            @PathVariable("usuarioId") Long usuarioId) {
        return reservaRepository.findAll().stream()
                .filter(r -> r.getUsuario() != null && r.getUsuario().getId().equals(usuarioId))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}/reservas")
    public List<Reserva> consultarReservas(
            @PathVariable("id") Long id) {
        return reservaRepository.findByAulaId(id);
    }
    
    @GetMapping("/mis-reservas")
    public ResponseEntity<List<Reserva>> misReservas(@RequestHeader("Authorization") String token) {
        User user = authService.getEmpleadoByToken(token);
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        List<Reserva> misReservas = reservaRepository.findAll().stream()
                .filter(r -> r.getUsuario() != null && r.getUsuario().getId().equals(user.getId()))
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(misReservas);
    }
    
    @PostMapping("/{aulaId}/bloquear")
    public ResponseEntity<?> bloquearSala(
            @PathVariable("aulaId") Long aulaId,
            @RequestBody Map<String, String> payload, 
            @RequestHeader("Authorization") String token) {

        try {
            User user = authService.getEmpleadoByToken(token);
            
            // Validar rol de Bibliotecario o Administrador
            if (user == null || (user.getRole() != User.Role.BIBLIOTECARIO && user.getRole() != User.Role.ADMIN)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para bloquear salas.");
            }

            Optional<Aula> aulaOpt = aulaRepository.findById(aulaId);
            if (aulaOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Aula no encontrada.");
            }

            // 1. Extraemos los textos del JSON directamente
            String fechaInicioStr = payload.get("fechaInicio");
            String fechaFinStr = payload.get("fechaFin");
            String motivo = payload.get("motivo");

            if (fechaInicioStr == null || fechaFinStr == null || motivo == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Faltan datos (fechaInicio, fechaFin o motivo).");
            }

            // 2. Añadimos los segundos si el frontend no los manda (para que no falle el parseo)
            if (fechaInicioStr.length() == 16) fechaInicioStr += ":00";
            if (fechaFinStr.length() == 16) fechaFinStr += ":00";

            // 3. Convertimos a LocalDateTime
            LocalDateTime fechaInicio = LocalDateTime.parse(fechaInicioStr);
            LocalDateTime fechaFin = LocalDateTime.parse(fechaFinStr);

            // 4. Creamos el objeto y lo guardamos
            BloqueoSala bloqueo = new BloqueoSala();
            bloqueo.setAula(aulaOpt.get());
            bloqueo.setFechaInicio(fechaInicio);
            bloqueo.setFechaFin(fechaFin);
            bloqueo.setMotivo(motivo);

            bloqueoSalaRepository.save(bloqueo);

            return ResponseEntity.ok("Sala bloqueada correctamente por motivo: " + motivo);
            
        } catch (java.time.format.DateTimeParseException e) {
            e.printStackTrace();
            // Si las fechas fallan, ahora nos devolverá esto
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Las fechas no tienen el formato correcto (esperado: YYYY-MM-DDTHH:MM:SS)");
        } catch (Exception e) {
            e.printStackTrace();
            // Si la base de datos se queja, nos devolverá el error exacto
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar en BD: " + e.getMessage());
        }
    }
}