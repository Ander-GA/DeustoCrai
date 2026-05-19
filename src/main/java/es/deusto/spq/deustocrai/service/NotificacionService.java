package es.deusto.spq.deustocrai.service;

import es.deusto.spq.deustocrai.entity.Prestamo;
import es.deusto.spq.deustocrai.entity.User;
import org.springframework.stereotype.Service;

@Service
public class NotificacionService {

    public void notificarAsignacionDesdeCola(User usuario, String recurso) {
        System.out.println("======================================");
        System.out.println("📧 NOTIFICACIÓN DEUSTO CRAI");
        System.out.println("Para: " + usuario.getEmail());
        System.out.println("Asunto: Recurso asignado desde cola de espera");
        System.out.println("Hola " + usuario.getNombre() + ",");
        System.out.println("El recurso '" + recurso + "' ha quedado disponible y se te ha asignado automáticamente.");
        System.out.println("======================================");
    }

    public void notificarRecordatorioDevolucion(Prestamo prestamo) {
        System.out.println("======================================");
        System.out.println("📧 RECORDATORIO DE DEVOLUCIÓN");
        System.out.println("Para: " + prestamo.getUsuario().getEmail());
        System.out.println("Asunto: Recuerda devolver tu préstamo");
        System.out.println("Hola " + prestamo.getUsuario().getNombre() + ",");
        System.out.println("Mañana vence el préstamo de: " + prestamo.getNombreRecursoHistorico());
        System.out.println("Fecha prevista: " + prestamo.getFechaDevolucionPrevista());
        System.out.println("======================================");
    }

    public void notificarPenalizacion(User usuario, int dias) {
    System.out.println("======================================");
    System.out.println("📧 NOTIFICACIÓN DE PENALIZACIÓN");
    System.out.println("Para: " + usuario.getEmail());
    System.out.println("Asunto: Penalización aplicada");
    System.out.println("Hola " + usuario.getNombre() + ",");
    System.out.println("Se te ha aplicado una penalización de " + dias + " días.");
    System.out.println("No podrás realizar nuevas reservas/préstamos hasta: " + usuario.getFechaFinPenalizacion());
    System.out.println("======================================");
}
}