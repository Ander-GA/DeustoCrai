package es.deusto.spq.deustocrai.service;

import es.deusto.spq.deustocrai.dao.LibroRepository;
import es.deusto.spq.deustocrai.entity.Libro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional; 

@Service
public class LibroService {

    @Autowired
    private LibroRepository libroRepository;

    public Libro anadirLibro(Libro libro) {
        libro.setDisponible(true); 
        return libroRepository.save(libro);
    }

    public boolean borrarLibro(Long id) {
        if (libroRepository.existsById(id)) {
            libroRepository.deleteById(id);
            return true; 
        }
        return false;
    }

    public List<Libro> listarLibros() {
        return libroRepository.findAll();
    }

    // --- NUEVA LÓGICA ---
    public Optional<Libro> obtenerLibroPorId(Long id) {
        return libroRepository.findById(id);
    }
}