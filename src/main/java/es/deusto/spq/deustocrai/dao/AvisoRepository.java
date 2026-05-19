package es.deusto.spq.deustocrai.dao;

import es.deusto.spq.deustocrai.entity.Aviso;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AvisoRepository extends JpaRepository<Aviso, Long> {

    List<Aviso> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    List<Aviso> findByUsuarioIdAndLeidoFalseOrderByFechaCreacionDesc(Long usuarioId);
}