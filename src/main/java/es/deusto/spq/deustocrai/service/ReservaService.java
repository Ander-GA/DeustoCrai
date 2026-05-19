package es.deusto.spq.deustocrai.service;

import es.deusto.spq.deustocrai.dao.ReservaRepository;
import es.deusto.spq.deustocrai.entity.Reserva;
import es.deusto.spq.deustocrai.entity.User;
import es.deusto.spq.deustocrai.dao.BloqueoSalaRepository;
import es.deusto.spq.deustocrai.entity.BloqueoSala;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final es.deusto.spq.deustocrai.dao.UserRepository userRepository; 
    private final BloqueoSalaRepository bloqueoSalaRepository;
    private final EmailService emailService; // NUEVO INYECTADO
    
    public ReservaService(ReservaRepository reservaRepository, 
                          es.deusto.spq.deustocrai.dao.UserRepository userRepository, 
                          BloqueoSalaRepository bloqueoSalaRepository,
                          EmailService emailService) {
        this.reservaRepository = reservaRepository;
        this.userRepository = userRepository;
        this.bloqueoSalaRepository = bloqueoSalaRepository;
        this.emailService = emailService;
    }

    public List<Reserva> getReservasPorAula(Long aulaId) {
        return reservaRepository.findByAulaId(aulaId);
    }

    public Optional<Reserva> realizarReserva(Reserva nueva) {
        Long aulaId = nueva.getAula().getId(); 
        
        List<BloqueoSala> bloqueos = bloqueoSalaRepository.findByAulaId(aulaId);
        boolean solapaConBloqueo = bloqueos.stream().anyMatch(b -> 
            nueva.getFechaHoraInicio().isBefore(b.getFechaFin()) && 
            b.getFechaInicio().isBefore(nueva.getFechaHoraFin())
        );

        if (solapaConBloqueo) {
            return Optional.empty();
        }

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
            .filter(r -> !r.getId().equals(id))
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
    
    @Transactional
    public Optional<Reserva> devolverSalaEarly(Long id, Long usuarioId) {
        Optional<Reserva> optReserva = reservaRepository.findById(id);
        if (optReserva.isEmpty()) return Optional.empty();
        Reserva reserva = optReserva.get();

        if (LocalDateTime.now().isBefore(reserva.getFechaHoraInicio())) {
            return Optional.empty(); 
        }

        if (reserva.getUsuario() == null || !reserva.getUsuario().getId().equals(usuarioId)) {
            return Optional.empty();
        }

        reserva.setFechaHoraFin(LocalDateTime.now());
        reserva.setDevuelta(true);
        return Optional.of(reservaRepository.save(reserva));
    }
    
    @Scheduled(fixedRate = 10000) 
    @Transactional
    public void aplicarPenalizacionesAutomaticas() {
        List<Reserva> expiradas = reservaRepository.findByDevueltaFalseAndFechaHoraFinBefore(LocalDateTime.now());

        for (Reserva r : expiradas) {
            User u = r.getUsuario();
            if (u != null && !u.isBloqueado()) {
                u.setBloqueado(true);
                u.setFechaFinPenalizacion(LocalDateTime.now().plusSeconds(30)); 
                userRepository.save(u);
                
                // Enviar correo de penalización
                String mensaje = "Hola " + u.getNombre() + ",\n\n"
                        + "Has sido penalizado por no devolver la sala '" + (r.getAula() != null ? r.getAula().getNombre() : "Reservada") 
                        + "' a tiempo. Estarás bloqueado temporalmente hasta: " + u.getFechaFinPenalizacion() + ".\n\n"
                        + "Un saludo,\nEquipo DeustoCRAI.";
                        
                emailService.enviarNotificacion(u.getEmail(), "Sanción aplicada - DeustoCRAI", mensaje);
            }
            r.setDevuelta(true);
            reservaRepository.save(r);
        }
    }

    // NUEVO: Tarea que se ejecuta cada minuto para enviar los avisos previos
    @Scheduled(fixedRate = 60000) 
    @Transactional
    public void notificarReservasProximas() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime enUnaHora = ahora.plusHours(1);

        List<Reserva> proximas = reservaRepository.findByDevueltaFalseAndAvisoEnviadoFalseAndFechaHoraInicioBetween(ahora, enUnaHora);

        for (Reserva r : proximas) {
            User u = r.getUsuario();
            if (u != null) {
                String tipoReserva = (r.getAula() != null) ? "Sala " + r.getAula().getNombre() : "Instalación";
                
                String mensaje = "Hola " + u.getNombre() + ",\n\n"
                        + "Te recordamos que tienes una reserva de tipo: " + tipoReserva + " que empieza a las: " + r.getFechaHoraInicio() + ".\n\n"
                        + "Recuerda devolverla a tiempo o confirmar tu salida en el sistema para evitar sanciones automáticas.\n\n"
                        + "Un saludo,\nEquipo DeustoCRAI.";

                emailService.enviarNotificacion(u.getEmail(), "Recordatorio: Tu reserva empieza en breve - DeustoCRAI", mensaje);
                
                r.setAvisoEnviado(true);
                reservaRepository.save(r);
            }
        }
    }
}