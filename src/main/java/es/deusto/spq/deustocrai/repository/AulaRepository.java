package es.deusto.spq.deustocrai.repository;

import es.deusto.spq.deustocrai.entity.Aula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AulaRepository extends JpaRepository<Aula, Long> {
}