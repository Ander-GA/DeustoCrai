package es.deusto.spq.deustocrai.facade;

import es.deusto.spq.deustocrai.entity.Prestamo;
import es.deusto.spq.deustocrai.entity.User;
import es.deusto.spq.deustocrai.service.AuthService;
import es.deusto.spq.deustocrai.service.PrestamoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {

    @Autowired
    private PrestamoService prestamoService;

    @Autowired
    private AuthService authService;

    @GetMapping("/mis-prestamos")
    // Añadimos ("Authorization") explícitamente
    public ResponseEntity<List<Prestamo>> misPrestamos(@RequestHeader("Authorization") String token) {
        User user = authService.getEmpleadoByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        
        return ResponseEntity.ok(prestamoService.obtenerPrestamosPorUsuario(user));
    }

    @PostMapping("/prestar/{libroId}")
    // Añadimos ("libroId") explícitamente
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
    // Añadimos ("prestamoId") explícitamente
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
}