package es.deusto.spq.deustocrai.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import es.deusto.spq.deustocrai.entity.InstalacionDeportiva;

public interface InstalacionRepository extends JpaRepository<InstalacionDeportiva, Long> {}