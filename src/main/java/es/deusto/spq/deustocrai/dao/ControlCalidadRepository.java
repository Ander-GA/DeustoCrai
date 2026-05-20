package es.deusto.spq.deustocrai.dao;

import es.deusto.spq.deustocrai.entity.ControlCalidad;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ControlCalidadRepository extends JpaRepository<ControlCalidad, Long> {

    List<ControlCalidad> findByMaterialIdOrderByFechaRevisionDesc(Long materialId);

    List<ControlCalidad> findByBibliotecarioIdOrderByFechaRevisionDesc(Long bibliotecarioId);
}