package es.deusto.spq.deustocrai.repository;

import es.deusto.spq.deustocrai.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByAulaId(Long aulaId);
}