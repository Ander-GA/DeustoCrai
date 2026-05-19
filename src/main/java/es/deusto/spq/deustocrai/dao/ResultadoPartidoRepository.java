package es.deusto.spq.deustocrai.dao;

import es.deusto.spq.deustocrai.entity.ResultadoPartido;
import es.deusto.spq.deustocrai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResultadoPartidoRepository extends JpaRepository<ResultadoPartido, Long> {
    List<ResultadoPartido> findByGanador(User ganador);
    List<ResultadoPartido> findByReservaUsuario(User usuario); // Todos los partidos que jugó el usuario que reservó
    boolean existsByReservaId(Long reservaId); // Para no registrar dos veces el mismo partido
}