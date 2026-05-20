package es.deusto.spq.deustocrai.dao;

import es.deusto.spq.deustocrai.entity.ReservaInstalacion;
import es.deusto.spq.deustocrai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReservaInstalacionRepository extends JpaRepository<ReservaInstalacion, Long> {
    
    // Método para ver las reservas por estado (ej. para el bibliotecario o el calendario)
    List<ReservaInstalacion> findByEstado(ReservaInstalacion.EstadoReserva estado);
    
    // Método para obtener las reservas que son de un usuario en concreto (para "Mis R.deportivas")
    List<ReservaInstalacion> findByUsuario(User usuario);
    
    // Método arreglado con guion bajo (_) para buscar por ID de instalación y estado
    List<ReservaInstalacion> findByInstalacion_IdAndEstado(Long instalacionId, ReservaInstalacion.EstadoReserva estado);
}