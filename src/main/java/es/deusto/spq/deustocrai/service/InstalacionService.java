package es.deusto.spq.deustocrai.service;

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
        if (reserva.getInstalacion() == null || reserva.getInstalacion().getId() == null) {
            return "Error: No se ha especificado la instalación deportiva.";
        }
        if (reserva.getFechaHoraInicio() == null || reserva.getFechaHoraFin() == null) {
            return "Error: Faltan las horas de inicio o fin de la reserva.";
        }

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime limite = ahora.plusDays(6);

        if (reserva.getFechaHoraInicio().isBefore(ahora)) {
            return "No puedes reservar en el pasado.";
        }
        if (reserva.getFechaHoraInicio().isAfter(limite)) {
            return "Solo puedes reservar con un máximo de 6 días de antelación.";
        }

        List<ReservaInstalacion> reservasAprobadas = reservaRepo.findByInstalacion_IdAndEstado(
                reserva.getInstalacion().getId(), 
                ReservaInstalacion.EstadoReserva.APROBADA
        );

        boolean ocupado = reservasAprobadas.stream().anyMatch(r -> 
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

    // --- NUEVO: Cancelar reserva del usuario ---
    public boolean cancelarReservaUsuario(Long reservaId, Long usuarioId) {
        Optional<ReservaInstalacion> opt = reservaRepo.findById(reservaId);
        if (opt.isPresent()) {
            ReservaInstalacion r = opt.get();
            // Comprobamos que la reserva pertenece a quien intenta borrarla
            if (r.getUsuario() != null && r.getUsuario().getId().equals(usuarioId)) {
                reservaRepo.delete(r);
                return true;
            }
        }
        return false;
    }

    // --- NUEVO: Modificar reserva del usuario ---
    public String modificarReservaUsuario(Long reservaId, Long usuarioId, LocalDateTime nuevaInicio, LocalDateTime nuevaFin) {
        Optional<ReservaInstalacion> opt = reservaRepo.findById(reservaId);
        if (opt.isEmpty()) return "Reserva no encontrada.";
        ReservaInstalacion r = opt.get();

        if (r.getUsuario() == null || !r.getUsuario().getId().equals(usuarioId)) {
            return "No tienes permiso para modificar esta reserva.";
        }
        if (r.getEstado() == ReservaInstalacion.EstadoReserva.APROBADA) {
            return "No se puede modificar una reserva ya aprobada.";
        }
        if (nuevaInicio == null || nuevaFin == null) return "Faltan fechas.";

        LocalDateTime ahora = LocalDateTime.now();
        if (nuevaInicio.isBefore(ahora)) return "No puedes poner una fecha en el pasado.";
        if (nuevaInicio.isAfter(ahora.plusDays(6))) return "Máximo 6 días de antelación.";
        if (nuevaInicio.isAfter(nuevaFin) || nuevaInicio.isEqual(nuevaFin)) return "La fecha de inicio debe ser anterior a la de fin.";

        List<ReservaInstalacion> aprobadas = reservaRepo.findByInstalacion_IdAndEstado(
                r.getInstalacion().getId(), 
                ReservaInstalacion.EstadoReserva.APROBADA
        );
        boolean ocupado = aprobadas.stream().anyMatch(aprobada -> 
            nuevaInicio.isBefore(aprobada.getFechaHoraFin()) && aprobada.getFechaHoraInicio().isBefore(nuevaFin)
        );
        if (ocupado) return "La instalación ya está aprobada para otro usuario en ese horario.";

        // Actualizamos fechas y devolvemos a estado PENDIENTE para revisión
        r.setFechaHoraInicio(nuevaInicio);
        r.setFechaHoraFin(nuevaFin);
        r.setEstado(ReservaInstalacion.EstadoReserva.PENDIENTE);
        reservaRepo.save(r);
        return "OK";
    }

    public List<Map<String, Object>> obtenerEventosCalendario() {
        List<ReservaInstalacion> aprobadas = reservaRepo.findByEstado(ReservaInstalacion.EstadoReserva.APROBADA);
        List<Map<String, Object>> eventos = new ArrayList<>();
        
        for (ReservaInstalacion res : aprobadas) {
            Map<String, Object> evento = new HashMap<>();
            evento.put("id", res.getId());
            evento.put("title", res.getInstalacion().getNombre() + " - Ocupada");
            evento.put("start", res.getFechaHoraInicio().toString());
            evento.put("end", res.getFechaHoraFin().toString());
            
            String color;
            if (res.getInstalacion().getTipo() != null && res.getInstalacion().getTipo().equalsIgnoreCase("FUTBOL")) {
                color = "#28a745"; 
            } else {
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