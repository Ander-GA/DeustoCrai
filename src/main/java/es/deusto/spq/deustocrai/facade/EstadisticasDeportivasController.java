package es.deusto.spq.deustocrai.facade;

import es.deusto.spq.deustocrai.entity.ResultadoPartido;
import es.deusto.spq.deustocrai.service.EstadisticasDeportivasService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deportes/stats")
public class EstadisticasDeportivasController {

    private final EstadisticasDeportivasService statsService;

    public EstadisticasDeportivasController(EstadisticasDeportivasService statsService) {
        this.statsService = statsService;
    }

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarPartido(@RequestBody ResultadoPartido resultado) {
        String msg = statsService.registrarResultado(resultado);
        if ("OK".equals(msg)) {
            return ResponseEntity.ok("Resultado registrado correctamente.");
        }
        return ResponseEntity.badRequest().body(msg);
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<Map<String, Object>>> getRanking() {
        return ResponseEntity.ok(statsService.obtenerRankingUsuarios());
    }
}