package es.deusto.spq.deustocrai.dao;

import es.deusto.spq.deustocrai.entity.Valoracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ValoracionRepository extends JpaRepository<Valoracion, Long> {
    
    List<Valoracion> findByRecursoIdOrderByFechaDesc(Long recursoId);
    
    boolean existsByUsuarioIdAndRecursoId(Long usuarioId, Long recursoId);
}