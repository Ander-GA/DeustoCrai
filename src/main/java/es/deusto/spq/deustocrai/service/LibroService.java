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

    public int borrarLibro(Long id) {
        // 1. Comprobamos si el libro existe
        if (!libroRepository.existsById(id)) {
            return -1; // 404
        }

        List<Prestamo> prestamos = prestamoRepository.findByRecursoId(id);

        // 2. Comprobamos si CUALQUIER préstamo está activo usando un Stream
        boolean estaPrestado = prestamos.stream().anyMatch(p -> 
            p.getEstado() == Prestamo.EstadoPrestamo.PENDIENTE_ENTREGA || 
            p.getEstado() == Prestamo.EstadoPrestamo.ENTREGADO
        );

        if (estaPrestado) {
            return 0; // Conflicto (409)
        }

        // 3. Cortamos la cuerda a todos los históricos y borramos el libro
        prestamos.forEach(p -> p.setRecurso(null));
        prestamoRepository.saveAll(prestamos); // Guardamos la lista entera de golpe, más eficiente

        libroRepository.deleteById(id);
        return 1; // 204 OK
    }

    public List<Libro> listarLibros() {
        return libroRepository.findAll();
    }

    public Optional<Libro> obtenerLibroPorId(Long id) {
        return libroRepository.findById(id);
    }
}