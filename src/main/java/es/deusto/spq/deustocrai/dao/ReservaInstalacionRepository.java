package es.deusto.spq.deustocrai.dao;
import org.springframework.data.jpa.repository.JpaRepository;
import es.deusto.spq.deustocrai.entity.ReservaInstalacion;
import java.util.List;

public interface ReservaInstalacionRepository extends JpaRepository<ReservaInstalacion, Long> {
    // Para que el bibliotecario vea las que están a la espera
    List<ReservaInstalacion> findByEstado(ReservaInstalacion.EstadoReserva estado);
    // Para ver si la pista ya está pillada en ese horario
    List<ReservaInstalacion> findByInstalacionIdAndEstado(Long instalacionId, ReservaInstalacion.EstadoReserva estado);
}
