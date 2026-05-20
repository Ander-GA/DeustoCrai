package es.deusto.spq.deustocrai.facade;

import es.deusto.spq.deustocrai.entity.ControlCalidad;
import es.deusto.spq.deustocrai.entity.ControlCalidad.EstadoControl;
import es.deusto.spq.deustocrai.entity.User;
import es.deusto.spq.deustocrai.service.AuthService;
import es.deusto.spq.deustocrai.service.ControlCalidadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/control-calidad")
public class ControlCalidadController {

    private final ControlCalidadService controlCalidadService;
    private final AuthService authService;

    public ControlCalidadController(
            ControlCalidadService controlCalidadService,
            AuthService authService
    ) {
        this.controlCalidadService = controlCalidadService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<?> registrarControl(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestParam Long materialId,
            @RequestParam(required = false) Long prestamoId,
            @RequestParam EstadoControl estado,
            @RequestParam(required = false, defaultValue = "") String observaciones
    ) {
        User bibliotecario = authService.getEmpleadoByToken(token);

        if (!esAdminOBibliotecario(bibliotecario)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tienes permisos para registrar controles de calidad.");
        }

        try {
            ControlCalidad control = controlCalidadService.registrarControl(
                    materialId,
                    prestamoId,
                    bibliotecario,
                    estado,
                    observaciones
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(control);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    private boolean esAdminOBibliotecario(User user) {
        return user != null &&
                (user.getRole() == User.Role.ADMIN ||
                 user.getRole() == User.Role.BIBLIOTECARIO);
    }
}