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

    // NUEVO MÉTODO PARA EL BIBLIOTECARIO: Obtener todos los préstamos del sistema
    public List<Prestamo> obtenerTodosLosPrestamos() {
        return prestamoRepository.findAll();
    }

    public Prestamo realizarPrestamo(User usuario, Long libroId) {
        Optional<Libro> libroOpt = libroRepository.findById(libroId);
        
        if (libroOpt.isPresent() && libroOpt.get().isDisponible()) {
            Libro libro = libroOpt.get();
            libro.setDisponible(false); // El libro se reserva automáticamente para que nadie más lo coja
            libroRepository.save(libro); 
            
            // Crea el préstamo (por defecto se pondrá en PENDIENTE_ENTREGA por el constructor)
            Prestamo prestamo = new Prestamo(usuario, libro);
            return prestamoRepository.save(prestamo);
        }
        return null; 
    }

    public boolean devolverPrestamo(User usuario, Long prestamoId) {
        Optional<Prestamo> prestamoOpt = prestamoRepository.findById(prestamoId);
        
        if (prestamoOpt.isPresent()) {
            Prestamo prestamo = prestamoOpt.get();
            
            // Validamos que el préstamo pertenezca al usuario y no esté ya devuelto
            if (prestamo.getUsuario().getId().equals(usuario.getId()) && prestamo.getEstado() != Prestamo.EstadoPrestamo.DEVUELTO) {
                Libro libro = (Libro) prestamo.getRecurso();
                libro.setDisponible(true); // El libro vuelve a estar disponible
                libroRepository.save(libro);
                
                prestamo.setEstado(Prestamo.EstadoPrestamo.DEVUELTO); // Marcamos como devuelto
                prestamoRepository.save(prestamo); // Actualizamos, NO borramos
                return true;
            }
        }
        return false;
    }

    public boolean cambiarEstadoPrestamo(Long prestamoId, Prestamo.EstadoPrestamo nuevoEstado) {
        Optional<Prestamo> prestamoOpt = prestamoRepository.findById(prestamoId);
        
        if (prestamoOpt.isPresent()) {
            Prestamo prestamo = prestamoOpt.get();
            prestamo.setEstado(nuevoEstado);
            
            // Si el bibliotecario marca como devuelto, tenemos que liberar el libro
            if (nuevoEstado == Prestamo.EstadoPrestamo.DEVUELTO) {
                Libro libro = (Libro) prestamo.getRecurso();
                libro.setDisponible(true);
                libroRepository.save(libro);
            }
            
            prestamoRepository.save(prestamo);
            return true;
        }
        return false;
    }
}