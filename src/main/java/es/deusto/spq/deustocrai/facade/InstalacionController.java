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

    @GetMapping("/instalaciones")
    public ResponseEntity<?> getInstalaciones() {
        return ResponseEntity.ok(instalacionRepo.findAll());
    }

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

    @GetMapping("/pendientes")
    public ResponseEntity<?> getPendientes(@RequestHeader("Authorization") String token) {
        User user = authService.getEmpleadoByToken(token);
        if (user == null || (user.getRole() != User.Role.BIBLIOTECARIO && user.getRole() != User.Role.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(reservaRepo.findByEstado(ReservaInstalacion.EstadoReserva.PENDIENTE));
    }

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

    @GetMapping("/eventos")
    public ResponseEntity<List<Map<String, Object>>> obtenerEventosCalendario() {
        return ResponseEntity.ok(instalacionService.obtenerEventosCalendario());
    }

    @GetMapping("/mis-reservas")
    public ResponseEntity<?> getMisReservas(@RequestHeader("Authorization") String token) {
        User user = authService.getEmpleadoByToken(token);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        List<ReservaInstalacion> misReservas = reservaRepo.findByUsuario(user);
        return ResponseEntity.ok(misReservas);
    }

    // --- NUEVO: Endpoint para Cancelar ---
    @DeleteMapping("/mis-reservas/{id}")
    public ResponseEntity<?> cancelarMiReserva(@PathVariable("id") Long id, @RequestHeader("Authorization") String token) {
        User user = authService.getEmpleadoByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (instalacionService.cancelarReservaUsuario(id, user.getId())) {
            return ResponseEntity.ok("Reserva cancelada correctamente.");
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No se pudo cancelar la reserva.");
    }

    // --- NUEVO: Endpoint para Modificar ---
    @PutMapping("/mis-reservas/{id}")
    public ResponseEntity<?> modificarMiReserva(
            @PathVariable("id") Long id, 
            @RequestHeader("Authorization") String token,
            @RequestBody ReservaInstalacion nuevosDatos) {
        
        User user = authService.getEmpleadoByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        String resultado = instalacionService.modificarReservaUsuario(id, user.getId(), nuevosDatos.getFechaHoraInicio(), nuevosDatos.getFechaHoraFin());
        
        if ("OK".equals(resultado)) {
            return ResponseEntity.ok("Reserva modificada correctamente.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resultado);
        }
    }
}