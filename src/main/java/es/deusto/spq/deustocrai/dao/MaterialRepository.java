package es.deusto.spq.deustocrai.dao;

import es.deusto.spq.deustocrai.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
}