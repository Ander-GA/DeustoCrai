package es.deusto.spq.deustocrai.facade;

import es.deusto.spq.deustocrai.entity.User;
import es.deusto.spq.deustocrai.service.AuthService;
import es.deusto.spq.deustocrai.service.AvisoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/avisos")
public class AvisoController {

    private final AvisoService avisoService;
    private final AuthService authService;

    public AvisoController(AvisoService avisoService, AuthService authService) {
        this.avisoService = avisoService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<?> obtenerMisAvisos(
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        User usuario = authService.getEmpleadoByToken(token);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido.");
        }

        return ResponseEntity.ok(avisoService.obtenerAvisosUsuario(usuario));
    }

    @GetMapping("/no-leidos")
    public ResponseEntity<?> obtenerAvisosNoLeidos(
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        User usuario = authService.getEmpleadoByToken(token);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido.");
        }

        return ResponseEntity.ok(avisoService.obtenerAvisosNoLeidos(usuario));
    }

    @PutMapping("/{avisoId}/leer")
    public ResponseEntity<?> marcarComoLeido(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable("avisoId") Long avisoId
    ) {
        User usuario = authService.getEmpleadoByToken(token);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido.");
        }

        try {
            return ResponseEntity.ok(avisoService.marcarComoLeido(avisoId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}