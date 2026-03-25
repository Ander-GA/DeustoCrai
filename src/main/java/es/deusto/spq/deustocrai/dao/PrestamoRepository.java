package es.deusto.spq.deustocrai.dao;

import es.deusto.spq.deustocrai.entity.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {
    
 
    List<Prestamo> findByUsuarioId(Long usuarioId);
}