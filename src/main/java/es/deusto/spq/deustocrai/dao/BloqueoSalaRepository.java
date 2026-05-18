package es.deusto.spq.deustocrai.dao;

import es.deusto.spq.deustocrai.entity.BloqueoSala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BloqueoSalaRepository extends JpaRepository<BloqueoSala, Long> {
    List<BloqueoSala> findByAulaId(Long aulaId);
}