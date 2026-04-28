package es.deusto.spq.deustocrai.service;

import es.deusto.spq.deustocrai.dao.ReservaRepository;
import es.deusto.spq.deustocrai.entity.Reserva;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;

    public ReservaService(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    public List<Reserva> getReservasPorAula(Long aulaId) {
        return reservaRepository.findByAulaId(aulaId);
    }

    public Optional<Reserva> realizarReserva(Reserva nueva) {
        Long aulaId = nueva.getAula().getId(); 
        List<Reserva> existentes = reservaRepository.findByAulaId(aulaId);
        
        boolean conflicto = existentes.stream().anyMatch(r -> 
            nueva.getFechaHoraInicio().isBefore(r.getFechaHoraFin()) && 
            r.getFechaHoraInicio().isBefore(nueva.getFechaHoraFin())
        );

        if (conflicto) {
            return Optional.empty();
        }

        return Optional.of(reservaRepository.save(nueva));
    }
    
    public List<Reserva> obtenerReservasActivas() {
        return reservaRepository.findReservasActivas();
    }

    // --- MÉTODOS DE CANCELAR Y EXTENDER CON SEGURIDAD ---

    @Transactional
    public boolean cancelarReserva(Long id, Long usuarioId) {
        Optional<Reserva> optReserva = reservaRepository.findById(id);
        
        if (optReserva.isPresent()) {
            Reserva reserva = optReserva.get();
            if (reserva.getUsuario() != null && reserva.getUsuario().getId().equals(usuarioId)) {
                reservaRepository.deleteById(id);
                return true;
            }
        }
        return false;
    }

    @Transactional
    public Optional<Reserva> extenderReserva(Long id, int minutosExtra, Long usuarioId) {
        Optional<Reserva> optReserva = reservaRepository.findById(id);
        
        if (optReserva.isEmpty()) {
            return Optional.empty(); 
        }

        Reserva reservaActual = optReserva.get();

        if (reservaActual.getUsuario() == null || !reservaActual.getUsuario().getId().equals(usuarioId)) {
            return Optional.empty();
        }

        if (reservaActual.getAula() == null) {
            return Optional.empty();
        }

        LocalDateTime nuevaFechaFin = reservaActual.getFechaHoraFin().plusMinutes(minutosExtra);

        List<Reserva> existentes = reservaRepository.findByAulaId(reservaActual.getAula().getId());
        
        boolean conflicto = existentes.stream()
            .filter(r -> !r.getId().equals(id)) // Ignorar la reserva actual
            .anyMatch(r -> 
                reservaActual.getFechaHoraInicio().isBefore(r.getFechaHoraFin()) && 
                r.getFechaHoraInicio().isBefore(nuevaFechaFin)
            );

        if (conflicto) {
            return Optional.empty();
        }

        reservaActual.setFechaHoraFin(nuevaFechaFin);
        return Optional.of(reservaRepository.save(reservaActual));
    }
}