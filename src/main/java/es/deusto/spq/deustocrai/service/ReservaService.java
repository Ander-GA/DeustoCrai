package es.deusto.spq.deustocrai.service;

import es.deusto.spq.deustocrai.dao.ReservaRepository;
import es.deusto.spq.deustocrai.entity.Reserva;
import org.springframework.stereotype.Service;
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
        // 1. Obtener reservas existentes para esa aula específica
        List<Reserva> existentes = reservaRepository.findByAulaId(nueva.getAula().getId());
        
        // 2. Lógica para evitar solapamientos
        boolean conflicto = existentes.stream().anyMatch(r -> 
            nueva.getFechaHoraInicio().isBefore(r.getFechaHoraFin()) && 
            r.getFechaHoraInicio().isBefore(nueva.getFechaHoraFin())
        );

        if (conflicto) {
            return Optional.empty(); // devuelve vacío si la sala está ocupada
        }

        // 3.bd
        return Optional.of(reservaRepository.save(nueva));
    }
}