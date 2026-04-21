package es.deusto.spq.deustocrai.facade;

import es.deusto.spq.deustocrai.entity.Prestamo;
import es.deusto.spq.deustocrai.entity.User;
import es.deusto.spq.deustocrai.service.AuthService;
import es.deusto.spq.deustocrai.service.PrestamoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;

import java.util.List;


//Se ha utilizado IA generativa para la ayuda de esqueleto de código, pero el código ha sido escrito y revisado por el equipo de desarrollo.
//Se han añadido comentarios para aclarar la funcionalidad de cada parte del código.
@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {

    @Autowired
    private PrestamoService prestamoService;

    @Autowired
    private AuthService authService;

    // --- Endpoints para el ESTUDIANTE ---

    @GetMapping("/mis-prestamos")
    public ResponseEntity<List<Prestamo>> misPrestamos(@RequestHeader("Authorization") String token) {
        User user = authService.getEmpleadoByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        
        return ResponseEntity.ok(prestamoService.obtenerPrestamosPorUsuario(user));
    }

    @PostMapping("/prestar/{libroId}")
    public ResponseEntity<?> prestarLibro(@RequestHeader("Authorization") String token, @PathVariable("libroId") Long libroId) {
        User user = authService.getEmpleadoByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Prestamo nuevoPrestamo = prestamoService.realizarPrestamo(user, libroId);
        
        if (nuevoPrestamo != null) {
            return ResponseEntity.ok(nuevoPrestamo);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El libro no está disponible");
        }
    }

    @PostMapping("/devolver/{prestamoId}")
    public ResponseEntity<?> devolverLibro(@RequestHeader("Authorization") String token, @PathVariable("prestamoId") Long prestamoId) {
        User user = authService.getEmpleadoByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        boolean devuelto = prestamoService.devolverPrestamo(user, prestamoId);
        
        if (devuelto) {
            return ResponseEntity.ok().body("{\"mensaje\": \"Libro devuelto con éxito\"}");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al devolver el libro");
        }
    }

    // --- Endpoints para el BIBLIOTECARIO ---

    @GetMapping("/todos")
    public ResponseEntity<List<Prestamo>> obtenerTodosLosPrestamos(@RequestHeader("Authorization") String token) {
        User user = authService.getEmpleadoByToken(token);
        
        if (user == null || (user.getRole() != User.Role.BIBLIOTECARIO && user.getRole() != User.Role.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(prestamoService.obtenerTodosLosPrestamos());
    }

    @PutMapping("/{prestamoId}/estado")
    public ResponseEntity<?> cambiarEstadoPrestamo(
            @RequestHeader("Authorization") String token, 
            @PathVariable("prestamoId") Long prestamoId,
            @RequestParam("nuevoEstado") Prestamo.EstadoPrestamo nuevoEstado) {
        
        User user = authService.getEmpleadoByToken(token);
        
        if (user == null || (user.getRole() != User.Role.BIBLIOTECARIO)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No tienes permisos para realizar esta acción");
        }

        boolean actualizado = prestamoService.cambiarEstadoPrestamo(prestamoId, nuevoEstado);
        
        if (actualizado) {
            return ResponseEntity.ok().body("{\"mensaje\": \"Estado actualizado con éxito\"}");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se encontró el préstamo");
        }
    }
    
    // --- NUEVOS ENDPOINTS SOLICITADOS ---

    @Operation(summary = "Prestar material", description = "Reserva un material disponible (portátil, cámara, etc.)")
    @PostMapping("/prestar-material/{materialId}")
    public ResponseEntity<?> prestarMaterial(
            // El truco para que Swagger no lo borre es no llamarlo "Authorization" en la interfaz gráfica
            @Parameter(in = ParameterIn.HEADER, name = "Token-Auth", required = true, description = "Pega aquí el Token")
            @RequestHeader(value = "Token-Auth", required = false) String tokenSwagger,
            @RequestHeader(value = "Authorization", required = false) String tokenReal,
            @PathVariable("materialId") Long materialId) {
        
        // Aceptamos el token ya sea por la vía normal (Frontend) o por el apaño de Swagger
        String token = (tokenReal != null && !tokenReal.isEmpty()) ? tokenReal : tokenSwagger;

        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No se ha recibido el token de autorización.");
        }
        
        User user = authService.getEmpleadoByToken(token);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido o caducado");
        }

        Prestamo nuevoPrestamo = prestamoService.realizarPrestamoMaterial(user, materialId);
        
        if (nuevoPrestamo != null) {
            return ResponseEntity.ok(nuevoPrestamo);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El material no está disponible");
        }
    }
    
    @Operation(summary = "Listar libros activos", description = "Lista los préstamos de libros que no están devueltos.")
    @GetMapping("/libros-activos")
    public ResponseEntity<?> obtenerLibrosActivos(
            @Parameter(in = ParameterIn.HEADER, name = "Token-Auth", required = true)
            @RequestHeader(value = "Token-Auth", required = false) String tokenSwagger,
            @RequestHeader(value = "Authorization", required = false) String tokenReal) {
        
        String token = (tokenReal != null && !tokenReal.isEmpty()) ? tokenReal : tokenSwagger;

        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No se ha recibido el token de autorización.");
        }

        User user = authService.getEmpleadoByToken(token);
        
        if (user == null || (user.getRole() != User.Role.BIBLIOTECARIO && user.getRole() != User.Role.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: Se requieren permisos de Bibliotecario o Admin.");
        }
        
        return ResponseEntity.ok(prestamoService.obtenerPrestamosLibrosActivos());
    }
}