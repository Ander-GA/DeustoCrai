package es.deusto.spq.deustocrai.service;

import es.deusto.spq.deustocrai.dao.InstalacionRepository;
import es.deusto.spq.deustocrai.dao.ReservaInstalacionRepository;
import es.deusto.spq.deustocrai.entity.ReservaInstalacion;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class InstalacionService {

    private final ReservaInstalacionRepository reservaRepo;

    public InstalacionService(ReservaInstalacionRepository reservaRepo) {
        this.reservaRepo = reservaRepo;
    }

    public String solicitarReserva(ReservaInstalacion reserva) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime limite = ahora.plusDays(6);

        // 1. Validar que no es en el pasado
        if (reserva.getFechaHoraInicio().isBefore(ahora)) {
            return "No puedes reservar en el pasado.";
        }

        // 2. REGLA DE LOS 6 DÍAS MÁXIMO
        if (reserva.getFechaHoraInicio().isAfter(limite)) {
            return "Solo puedes reservar con un máximo de 6 días de antelación.";
        }

        // 3. Comprobar que no haya una reserva APROBADA en ese mismo horario para esa pista
        boolean ocupado = reservaRepo.findByInstalacionIdAndEstado(reserva.getInstalacion().getId(), ReservaInstalacion.EstadoReserva.APROBADA)
            .stream().anyMatch(r -> 
                reserva.getFechaHoraInicio().isBefore(r.getFechaHoraFin()) && 
                r.getFechaHoraInicio().isBefore(reserva.getFechaHoraFin())
            );

        if (ocupado) {
            return "La instalación ya está aprobada para otro usuario en ese horario.";
        }

        reserva.setEstado(ReservaInstalacion.EstadoReserva.PENDIENTE);
        reservaRepo.save(reserva);
        return "OK";
    }

    public boolean procesarSolicitud(Long reservaId, ReservaInstalacion.EstadoReserva nuevoEstado) {
        Optional<ReservaInstalacion> opt = reservaRepo.findById(reservaId);
        if (opt.isPresent()) {
            ReservaInstalacion r = opt.get();
            r.setEstado(nuevoEstado);
            reservaRepo.save(r);
            return true;
        }
        return false;
    }

    public List<Map<String, Object>> obtenerEventosCalendario() {
        // Solo mostramos en el calendario las reservas que ya están aprobadas
        List<ReservaInstalacion> aprobadas = reservaRepo.findByEstado(ReservaInstalacion.EstadoReserva.APROBADA);
        List<Map<String, Object>> eventos = new ArrayList<>();
        
        for (ReservaInstalacion res : aprobadas) {
            Map<String, Object> evento = new HashMap<>();
            evento.put("id", res.getId());
            evento.put("title", res.getInstalacion().getNombre() + " - Ocupada");
            // LocalDateTime.toString() formatea automáticamente a ISO 8601 (ej: 2026-05-20T10:00:00)
            evento.put("start", res.getFechaHoraInicio().toString());
            evento.put("end", res.getFechaHoraFin().toString());
            
            // Asignar colores por tipo y pista
            String color;
            if ("FUTBOL".equalsIgnoreCase(res.getInstalacion().getTipo())) {
                color = "#28a745"; // Verde para el campo de fútbol
            } else {
                // Para las pistas de pádel u otros, asignamos un color distinto basado en su ID
                String[] colores = {"#007bff", "#dc3545", "#fd7e14", "#6f42c1", "#17a2b8"};
                int indiceColor = (int) (res.getInstalacion().getId() % colores.length);
                color = colores[indiceColor];
            }
            evento.put("color", color);
            
            eventos.add(evento);
        }
        return eventos;
    }
}