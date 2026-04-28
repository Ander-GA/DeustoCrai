package es.deusto.spq.deustocrai.dao;

import es.deusto.spq.deustocrai.entity.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {
    
    List<Prestamo> findByUsuarioId(Long usuarioId);
    
    //Buscar todos los préstamos atados a un recurso específico
    List<Prestamo> findByRecursoId(Long recursoId);
    
    @Query("SELECT p FROM Prestamo p WHERE p.estado != 'DEVUELTO' AND TYPE(p.recurso) = Libro")
    List<Prestamo> findLibrosPrestadosActivos();
    
    List<Prestamo> findByUsuarioIdAndEstado(Long usuarioId, Prestamo.EstadoPrestamo estado);
}