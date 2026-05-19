package es.deusto.spq.deustocrai.service;

import es.deusto.spq.deustocrai.dao.PrestamoRepository;
import es.deusto.spq.deustocrai.entity.Prestamo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class RecordatorioPrestamoScheduler {

    private final PrestamoRepository prestamoRepository;
    private final NotificacionService notificacionService;

    public RecordatorioPrestamoScheduler(
            PrestamoRepository prestamoRepository,
            NotificacionService notificacionService
    ) {
        this.prestamoRepository = prestamoRepository;
        this.notificacionService = notificacionService;
    }

    @Scheduled(fixedRate = 60000)
    public void enviarRecordatoriosDemo() {
        LocalDate manana = LocalDate.now().plusDays(1);

        for (Prestamo prestamo : prestamoRepository.findAll()) {
            if (
                    prestamo.getFechaDevolucionPrevista() != null &&
                    prestamo.getFechaDevolucionPrevista().equals(manana) &&
                    prestamo.getFechaDevolucionReal() == null
            ) {
                notificacionService.notificarRecordatorioDevolucion(prestamo);
            }
        }
    }
}