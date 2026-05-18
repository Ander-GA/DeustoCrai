package es.deusto.spq.deustocrai.dao;

import es.deusto.spq.deustocrai.entity.AbstractRecurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AbstractRecursoRepository extends JpaRepository<AbstractRecurso, Long> {
}
