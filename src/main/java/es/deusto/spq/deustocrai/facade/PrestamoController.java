package es.deusto.spq.deustocrai.facade;

import es.deusto.spq.deustocrai.dao.LibroRepository;
import es.deusto.spq.deustocrai.dao.PrestamoRepository;
import es.deusto.spq.deustocrai.entity.Libro;
import es.deusto.spq.deustocrai.entity.Prestamo;
import es.deusto.spq.deustocrai.entity.User;
import es.deusto.spq.deustocrai.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {

    @Autowired private PrestamoRepository prestamoRepository;
    @Autowired private LibroRepository libroRepository;
    @Autowired private AuthService authService;

    // Obtener los préstamos del usuario activo
    @GetMapping("/mis-prestamos")
    public ResponseEntity<List<Prestamo>> misPrestamos(@RequestHeader("Authorization") String token) {
        User user = authService.getEmpleadoByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(prestamoRepository.findByUsuario(user));
    }

    // Reservar / Prestar un libro
    @PostMapping("/prestar/{libroId}")
    public ResponseEntity<?> prestarLibro(@RequestHeader("Authorization") String token, @PathVariable Long libroId) {
        User user = authService.getEmpleadoByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Libro> libroOpt = libroRepository.findById(libroId);
        if (libroOpt.isPresent() && libroOpt.get().isDisponible()) {
            Libro libro = libroOpt.get();
            libro.setDisponible(false); 
            libroRepository.save(libro);
            
            Prestamo prestamo = new Prestamo(user, libro);
            prestamoRepository.save(prestamo);
            return ResponseEntity.ok(prestamo);
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body("El libro no está disponible");
    }

    // Devolver un libro
    @PostMapping("/devolver/{prestamoId}")
    public ResponseEntity<?> devolverLibro(@RequestHeader("Authorization") String token, @PathVariable Long prestamoId) {
        User user = authService.getEmpleadoByToken(token);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Optional<Prestamo> prestamoOpt = prestamoRepository.findById(prestamoId);
        if (prestamoOpt.isPresent()) {
            Prestamo prestamo = prestamoOpt.get();
            if (!prestamo.getUsuario().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // No es el dueño del préstamo
            }

            Libro libro = (Libro) prestamo.getRecurso();
            libro.setDisponible(true);
            libroRepository.save(libro);

            prestamoRepository.delete(prestamo);
            return ResponseEntity.ok().body("{\"mensaje\": \"Libro devuelto con éxito\"}");
        }
        return ResponseEntity.notFound().build();
    }
}