package es.deusto.spq.deustocrai.dao;

import es.deusto.spq.deustocrai.entity.ColaEspera;
import es.deusto.spq.deustocrai.entity.ColaEspera.EstadoCola;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ColaEsperaRepository extends JpaRepository<ColaEspera, Long> {

    List<ColaEspera> findByRecursoIdAndEstadoOrderByFechaEntradaAsc(Long recursoId, EstadoCola estado);

    Optional<ColaEspera> findFirstByRecursoIdAndEstadoOrderByFechaEntradaAsc(Long recursoId, EstadoCola estado);

    boolean existsByUsuarioIdAndRecursoIdAndEstado(Long usuarioId, Long recursoId, EstadoCola estado);
}