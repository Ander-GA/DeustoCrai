package es.deusto.spq.deustocrai.facade;

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

    
    @GetMapping("/{id}")
    public ResponseEntity<Libro> obtenerDetallesLibro(@PathVariable("id") Long id) {
        Optional<Libro> libro = libroService.obtenerLibroPorId(id);
        
        if (libro.isPresent()) {
            return ResponseEntity.ok(libro.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}