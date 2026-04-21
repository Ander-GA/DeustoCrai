package es.deusto.spq.deustocrai.facade;

import es.deusto.spq.deustocrai.dao.LibroRepository;
import es.deusto.spq.deustocrai.entity.Libro;
import es.deusto.spq.deustocrai.service.LibroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/libros")
public class LibroController {

    @Autowired
    private LibroService libroService;

    @Autowired
    private LibroRepository libroRepository; 

 
    @GetMapping
    public List<Libro> listarLibros() {
        return libroService.listarLibros();
    }

   
    @GetMapping("/buscar")
    public List<Libro> buscarLibros(@RequestParam("q") String q) {
        return libroRepository.findByTituloContainingIgnoreCase(q);
    }

   
    @GetMapping("/{id}")
    public ResponseEntity<Libro> obtenerDetallesLibro(@PathVariable("id") Long id) {
        Optional<Libro> libro = libroService.obtenerLibroPorId(id);
        
        if (libro.isPresent()) {
            return ResponseEntity.ok(libro.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

   
    @PostMapping
    public ResponseEntity<Libro> anadirLibro(@RequestBody Libro libro) {
        Libro nuevo = libroService.anadirLibro(libro);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

  
    @DeleteMapping("/{id}")
    public ResponseEntity<String> borrarLibro(@PathVariable("id") Long id) {
        int resultado = libroService.borrarLibro(id);
        
        if (resultado == 1) {
            return ResponseEntity.noContent().build(); // 204 OK (Se borró)
        } else if (resultado == 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No se puede borrar el libro porque actualmente se encuentra prestado."); 
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }
}