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
}