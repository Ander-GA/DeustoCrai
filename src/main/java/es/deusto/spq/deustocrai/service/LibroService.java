package es.deusto.spq.deustocrai.service;

import es.deusto.spq.deustocrai.dao.LibroRepository;
import es.deusto.spq.deustocrai.dao.PrestamoRepository;
import es.deusto.spq.deustocrai.entity.Libro;
import es.deusto.spq.deustocrai.entity.Prestamo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional; 

@Service
public class LibroService {

    @Autowired
    private LibroRepository libroRepository;
    
    @Autowired
    private PrestamoRepository prestamoRepository;

    public Libro anadirLibro(Libro libro) {
        libro.setDisponible(true); 
        return libroRepository.save(libro);
    }

    public boolean borrarLibro(Long id) {
        if (!libroRepository.existsById(id)) {
            return false;
        }

        // 1. Buscamos todos los préstamos vinculados a este libro
        List<Prestamo> prestamos = prestamoRepository.findByRecursoId(id);
        
        // 2. Rompemos la relación (ponemos el recurso a null)
        for (Prestamo prestamo : prestamos) {
            prestamo.setRecurso(null);
            prestamoRepository.save(prestamo); // Actualizamos el recibo
        }

        // 3. Borramos el libro físicamente (liberando el ISBN)
        libroRepository.deleteById(id);
        return true;
    }

    public List<Libro> listarLibros() {
        return libroRepository.findAll();
    }

    public Optional<Libro> obtenerLibroPorId(Long id) {
        return libroRepository.findById(id);
    }
}