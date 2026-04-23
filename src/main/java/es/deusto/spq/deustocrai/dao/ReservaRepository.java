package es.deusto.spq.deustocrai.dao;

import es.deusto.spq.deustocrai.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByAulaId(Long aulaId);
    
    @Query("SELECT r FROM Reserva r WHERE r.fechaHoraFin > CURRENT_TIMESTAMP")
    List<Reserva> findReservasActivas();
}

