package es.deusto.spq.deustocrai.service;

import es.deusto.spq.deustocrai.dao.*;
import es.deusto.spq.deustocrai.entity.Prestamo;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class EstadisticasService {

    private final PrestamoRepository prestamoRepository;
    private final UserRepository userRepository;
    private final LibroRepository libroRepository;
    private final MaterialRepository materialRepository;
    private final ReservaRepository reservaRepository;
    private final AvisoRepository avisoRepository;

    public EstadisticasService(
            PrestamoRepository prestamoRepository,
            UserRepository userRepository,
            LibroRepository libroRepository,
            MaterialRepository materialRepository,
            ReservaRepository reservaRepository,
            AvisoRepository avisoRepository
    ) {
        this.prestamoRepository = prestamoRepository;
        this.userRepository = userRepository;
        this.libroRepository = libroRepository;
        this.materialRepository = materialRepository;
        this.reservaRepository = reservaRepository;
        this.avisoRepository = avisoRepository;
    }

    public Map<String, Object> obtenerResumen() {
        Map<String, Object> resumen = new HashMap<>();

        long prestamosTotales = prestamoRepository.count();
        long usuariosTotales = userRepository.count();
        long librosTotales = libroRepository.count();
        long materialesTotales = materialRepository.count();
        long reservasTotales = reservaRepository.count();
        long avisosTotales = avisoRepository.count();

        long prestamosActivos = prestamoRepository.findAll()
                .stream()
                .filter(p -> p.getFechaDevolucionReal() == null)
                .count();

        long usuariosPenalizados = userRepository.findAll()
                .stream()
                .filter(u -> u.isBloqueado())
                .count();

        long avisosNoLeidos = avisoRepository.findAll()
                .stream()
                .filter(a -> !a.isLeido())
                .count();

        Map<String, Long> recursosSolicitados = new HashMap<>();

        for (Prestamo prestamo : prestamoRepository.findAll()) {
            String nombre = prestamo.getNombreRecursoHistorico();

            if (nombre == null && prestamo.getRecurso() != null) {
                nombre = prestamo.getRecurso().getTitulo();
            }

            if (nombre != null) {
                recursosSolicitados.put(
                        nombre,
                        recursosSolicitados.getOrDefault(nombre, 0L) + 1
                );
            }
        }

        resumen.put("prestamosTotales", prestamosTotales);
        resumen.put("prestamosActivos", prestamosActivos);
        resumen.put("usuariosTotales", usuariosTotales);
        resumen.put("usuariosPenalizados", usuariosPenalizados);
        resumen.put("librosTotales", librosTotales);
        resumen.put("materialesTotales", materialesTotales);
        resumen.put("reservasTotales", reservasTotales);
        resumen.put("avisosTotales", avisosTotales);
        resumen.put("avisosNoLeidos", avisosNoLeidos);
        resumen.put("recursosSolicitados", recursosSolicitados);

        return resumen;
    }
}