package es.deusto.spq.deustocrai.facade;

import es.deusto.spq.deustocrai.dao.UserRepository;
import es.deusto.spq.deustocrai.entity.User;
import es.deusto.spq.deustocrai.service.AuthService;
import es.deusto.spq.deustocrai.service.PenalizacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/penalizaciones")
public class PenalizacionController {

    private final PenalizacionService penalizacionService;
    private final AuthService authService;
    private final UserRepository userRepository;

    public PenalizacionController(
            PenalizacionService penalizacionService,
            AuthService authService,
            UserRepository userRepository
    ) {
        this.penalizacionService = penalizacionService;
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Listar todos los usuarios para gestión de penalizaciones")
    @GetMapping("/usuarios")
    public ResponseEntity<?> listarUsuarios(
            @Parameter(in = ParameterIn.HEADER, name = "Authorization", required = true)
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        User admin = authService.getEmpleadoByToken(token);

        if (!esAdminOBibliotecario(admin)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tienes permisos para ver usuarios.");
        }

        return ResponseEntity.ok(userRepository.findAll());
    }

    @Operation(summary = "Aplicar penalización a un usuario")
    @PutMapping("/{userId}")
    public ResponseEntity<?> aplicarPenalizacion(
            @Parameter(in = ParameterIn.HEADER, name = "Authorization", required = true)
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long userId,
            @RequestParam(defaultValue = "7") int dias
    ) {
        User admin = authService.getEmpleadoByToken(token);

        if (!esAdminOBibliotecario(admin)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tienes permisos para aplicar penalizaciones.");
        }

        try {
            return ResponseEntity.ok(
                    penalizacionService.aplicarPenalizacion(userId, dias)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @Operation(summary = "Eliminar penalización de un usuario")
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> eliminarPenalizacion(
            @Parameter(in = ParameterIn.HEADER, name = "Authorization", required = true)
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Long userId
    ) {
        User admin = authService.getEmpleadoByToken(token);

        if (!esAdminOBibliotecario(admin)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tienes permisos para eliminar penalizaciones.");
        }

        try {
            return ResponseEntity.ok(
                    penalizacionService.eliminarPenalizacion(userId)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @Operation(summary = "Listar usuarios penalizados")
    @GetMapping
    public ResponseEntity<?> listarPenalizados(
            @Parameter(in = ParameterIn.HEADER, name = "Authorization", required = true)
            @RequestHeader(value = "Authorization", required = false) String token
    ) {
        User admin = authService.getEmpleadoByToken(token);

        if (!esAdminOBibliotecario(admin)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tienes permisos para ver penalizaciones.");
        }

        return ResponseEntity.ok(penalizacionService.obtenerUsuariosPenalizados());
    }

    private boolean esAdminOBibliotecario(User user) {
        return user != null &&
                (user.getRole() == User.Role.ADMIN ||
                 user.getRole() == User.Role.BIBLIOTECARIO);
    }
}