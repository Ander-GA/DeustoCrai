package es.deusto.spq.deustocrai.service;

import es.deusto.spq.deustocrai.dao.LibroRepository;
import es.deusto.spq.deustocrai.dao.PrestamoRepository;
import es.deusto.spq.deustocrai.entity.Libro;
import es.deusto.spq.deustocrai.entity.Prestamo;
import es.deusto.spq.deustocrai.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PrestamoService {

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private LibroRepository libroRepository;

    public List<Prestamo> obtenerPrestamosPorUsuario(User usuario) {

        return prestamoRepository.findByUsuarioId(usuario.getId());
    }

    public Prestamo realizarPrestamo(User usuario, Long libroId) {
        Optional<Libro> libroOpt = libroRepository.findById(libroId);
        
        // Si el libro existe y está disponible
        if (libroOpt.isPresent() && libroOpt.get().isDisponible()) {
            Libro libro = libroOpt.get();
            libro.setDisponible(false); 
            libroRepository.save(libro); 
            
            Prestamo prestamo = new Prestamo(usuario, libro);
            return prestamoRepository.save(prestamo);
        }
        return null; 
    }

    public boolean devolverPrestamo(User usuario, Long prestamoId) {
        Optional<Prestamo> prestamoOpt = prestamoRepository.findById(prestamoId);
        
        if (prestamoOpt.isPresent()) {
            Prestamo prestamo = prestamoOpt.get();
            
            if (prestamo.getUsuario().getId().equals(usuario.getId())) {
                Libro libro = (Libro) prestamo.getRecurso();
                libro.setDisponible(true); 
                libroRepository.save(libro);
                
                prestamoRepository.delete(prestamo); 
                return true;
            }
        }
        return false;
    }
}