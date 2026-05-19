package es.deusto.spq.deustocrai.facade;

import es.deusto.spq.deustocrai.entity.User;
import es.deusto.spq.deustocrai.service.AuthService;
import es.deusto.spq.deustocrai.service.EstadisticasService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/estadisticas")
public class EstadisticasController {

    private final EstadisticasService estadisticasService;
    private final AuthService authService;

    public EstadisticasController(
            EstadisticasService estadisticasService,
            AuthService authService
    ) {
        this.estadisticasService = estadisticasService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<?> obtenerEstadisticas(
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        User usuario = authService.getEmpleadoByToken(token);

        if (!esAdminOBibliotecario(usuario)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tienes permisos para ver estadísticas.");
        }

        return ResponseEntity.ok(estadisticasService.obtenerResumen());
    }

    private boolean esAdminOBibliotecario(User user) {
        return user != null &&
                (user.getRole() == User.Role.ADMIN ||
                 user.getRole() == User.Role.BIBLIOTECARIO);
    }
}