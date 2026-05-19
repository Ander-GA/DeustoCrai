package es.deusto.spq.deustocrai.facade;

import es.deusto.spq.deustocrai.entity.ReservaInstalacion;
import es.deusto.spq.deustocrai.entity.User;
import es.deusto.spq.deustocrai.service.AuthService;
import es.deusto.spq.deustocrai.service.InstalacionService;
import es.deusto.spq.deustocrai.dao.InstalacionRepository;
import es.deusto.spq.deustocrai.dao.ReservaInstalacionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deportes")
public class InstalacionController {

    private final InstalacionService instalacionService;
    private final InstalacionRepository instalacionRepo;
    private final ReservaInstalacionRepository reservaRepo;
    private final AuthService authService;

    public InstalacionController(InstalacionService instalacionService, InstalacionRepository instalacionRepo, ReservaInstalacionRepository reservaRepo, AuthService authService) {
        this.instalacionService = instalacionService;
        this.instalacionRepo = instalacionRepo;
        this.reservaRepo = reservaRepo;
        this.authService = authService;
    }

    // Listar las pistas disponibles para el frontend
    @GetMapping("/instalaciones")
    public ResponseEntity<?> getInstalaciones() {
        return ResponseEntity.ok(instalacionRepo.findAll());
    }

    // Un estudiante solicita la pista
    @PostMapping("/solicitar")
    public ResponseEntity<?> solicitarReserva(@RequestHeader("Authorization") String token, @RequestBody ReservaInstalacion reserva) {
        User user = authService.getEmpleadoByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        reserva.setUsuario(user);
        String resultado = instalacionService.solicitarReserva(reserva);
        
        if ("OK".equals(resultado)) {
            return ResponseEntity.ok("Solicitud enviada. A la espera de aprobación.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultado);
        }
    }

    // El bibliotecario ve las que están "PENDIENTES"
    @GetMapping("/pendientes")
    public ResponseEntity<?> getPendientes(@RequestHeader("Authorization") String token) {
        User user = authService.getEmpleadoByToken(token);
        if (user == null || (user.getRole() != User.Role.BIBLIOTECARIO && user.getRole() != User.Role.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(reservaRepo.findByEstado(ReservaInstalacion.EstadoReserva.PENDIENTE));
    }

    // El bibliotecario acepta o rechaza
    @PutMapping("/procesar/{id}")
    public ResponseEntity<?> procesar(@RequestHeader("Authorization") String token, @PathVariable("id") Long id, @RequestParam("estado") ReservaInstalacion.EstadoReserva estado) {
        User user = authService.getEmpleadoByToken(token);
        if (user == null || user.getRole() != User.Role.BIBLIOTECARIO) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (instalacionService.procesarSolicitud(id, estado)) {
            return ResponseEntity.ok("Reserva actualizada a: " + estado);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    // Obtener los eventos para el calendario
    @GetMapping("/eventos")
    public ResponseEntity<List<Map<String, Object>>> obtenerEventosCalendario() {
        return ResponseEntity.ok(instalacionService.obtenerEventosCalendario());
    }
}