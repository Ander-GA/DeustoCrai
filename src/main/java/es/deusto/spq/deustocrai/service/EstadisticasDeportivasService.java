package es.deusto.spq.deustocrai.service;

import es.deusto.spq.deustocrai.dao.ResultadoPartidoRepository;
import es.deusto.spq.deustocrai.dao.UserRepository;
import es.deusto.spq.deustocrai.entity.ResultadoPartido;
import es.deusto.spq.deustocrai.entity.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EstadisticasDeportivasService {

    private final ResultadoPartidoRepository resultadoRepo;
    private final UserRepository userRepo;

    public EstadisticasDeportivasService(ResultadoPartidoRepository resultadoRepo, UserRepository userRepo) {
        this.resultadoRepo = resultadoRepo;
        this.userRepo = userRepo;
    }

    public String registrarResultado(ResultadoPartido resultado) {
        if (resultadoRepo.existsByReservaId(resultado.getReserva().getId())) {
            return "El resultado de este partido ya ha sido registrado.";
        }
        
        // Determinar quién ganó lógicamente si no viene seteado
        if (resultado.getPuntosLocal() > resultado.getPuntosVisitante()) {
            resultado.setGanador(resultado.getReserva().getUsuario());
        } // Si es empate o gana el visitante, requeriría más lógica de equipo, lo dejamos simple.

        resultadoRepo.save(resultado);
        return "OK";
    }

    public List<Map<String, Object>> obtenerRankingUsuarios() {
        List<User> todosLosUsuarios = userRepo.findAll();
        List<Map<String, Object>> ranking = new ArrayList<>();

        for (User u : todosLosUsuarios) {
            // Partidos donde este usuario reservó la pista
            List<ResultadoPartido> partidosJugados = resultadoRepo.findByReservaUsuario(u);
            List<ResultadoPartido> partidosGanados = resultadoRepo.findByGanador(u);

            int totalJugados = partidosJugados.size();
            if (totalJugados == 0) continue; // Si no ha jugado, no sale en el ranking

            int totalGanados = partidosGanados.size();
            int totalPerdidos = totalJugados - totalGanados;
            
            // Calcular Win Rate (%)
            double winRate = Math.round(((double) totalGanados / totalJugados) * 100.0 * 10.0) / 10.0;

            // Calcular puntos totales a favor
            int puntosAFavor = partidosJugados.stream().mapToInt(ResultadoPartido::getPuntosLocal).sum();

            Map<String, Object> stats = new HashMap<>();
            stats.put("usuarioId", u.getId());
            stats.put("nombre", u.getNombre());
            stats.put("partidosJugados", totalJugados);
            stats.put("victorias", totalGanados);
            stats.put("derrotas", totalPerdidos);
            stats.put("winRate", winRate);
            stats.put("puntosAnotados", puntosAFavor);

            ranking.add(stats);
        }

        // Ordenar el ranking por porcentaje de victorias (de mayor a menor)
        ranking.sort((a, b) -> Double.compare((Double) b.get("winRate"), (Double) a.get("winRate")));

        return ranking;
    }
}