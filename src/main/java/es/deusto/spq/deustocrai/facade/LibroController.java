package es.deusto.spq.deustocrai.facade;

import es.deusto.spq.deustocrai.entity.Libro;
import es.deusto.spq.deustocrai.service.LibroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/libros")
public class LibroController {

    @Autowired
    private LibroService libroService;

    @PostMapping
    public ResponseEntity<Libro> anadirLibro(@RequestBody Libro libro) {
        // Delegamos la creación al servicio
        Libro nuevo = libroService.anadirLibro(libro);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrarLibro(@PathVariable Long id) {
        // El servicio nos dice si lo ha podido borrar o no
        boolean borrado = libroService.borrarLibro(id);
        
        if (borrado) {
            return ResponseEntity.noContent().build(); 
        } else {
            return ResponseEntity.notFound().build(); 
        }
    }

    @GetMapping
    public List<Libro> listarLibros() {
        return libroService.listarLibros();
    }
    @GetMapping("/buscar")
    public List<Libro> buscarLibros(@RequestParam String q) {
        return libroService.buscarLibros(q); // Usamos el servicio en lugar del repositorio
    }
}