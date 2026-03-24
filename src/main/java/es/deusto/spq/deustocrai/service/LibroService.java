package es.deusto.spq.deustocrai.service;

import es.deusto.spq.deustocrai.dao.LibroRepository;
import es.deusto.spq.deustocrai.entity.Libro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LibroService {

    @Autowired
    private LibroRepository libroRepository;

    // Lógica para añadir un libro
    public Libro anadirLibro(Libro libro) {
        libro.setDisponible(true); 
        return libroRepository.save(libro);
    }

    // Lógica para borrar un libro comprobando si existe
    public boolean borrarLibro(Long id) {
        if (libroRepository.existsById(id)) {
            libroRepository.deleteById(id);
            return true; 
        }
        return false;
    }

    // Lógica para listar todos los libros
    public List<Libro> listarLibros() {
        return libroRepository.findAll();
    }
}