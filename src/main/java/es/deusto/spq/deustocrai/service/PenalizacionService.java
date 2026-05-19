package es.deusto.spq.deustocrai.service;

import es.deusto.spq.deustocrai.dao.UserRepository;
import es.deusto.spq.deustocrai.entity.User;
import es.deusto.spq.deustocrai.entity.Aviso;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PenalizacionService {

    private final UserRepository userRepository;
    private final NotificacionService notificacionService;
    private final AvisoService avisoService;

    public PenalizacionService(
            UserRepository userRepository,
            NotificacionService notificacionService,
            AvisoService avisoService
    ) {
        this.userRepository = userRepository;
        this.notificacionService = notificacionService;
        this.avisoService = avisoService;
    }

    public User aplicarPenalizacion(Long userId, int dias) {
        User usuario = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setBloqueado(true);
        usuario.setFechaFinPenalizacion(
            java.time.LocalDateTime.now().plusDays(dias)
        );

        User usuarioGuardado = userRepository.save(usuario);

        notificacionService.notificarPenalizacion(
                usuarioGuardado,
                dias
        );

        avisoService.crearAviso(
            usuarioGuardado,
            Aviso.TipoAviso.PENALIZACION,
            "Penalización aplicada",
            "Se te ha aplicado una penalización de " + dias + " días. No podrás realizar nuevas reservas o préstamos hasta " + usuarioGuardado.getFechaFinPenalizacion()
        );

        return usuarioGuardado;
    }

    public User eliminarPenalizacion(Long userId) {
        User usuario = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setBloqueado(false);
        usuario.setFechaFinPenalizacion(null);

        return userRepository.save(usuario);
    }

    public List<User> obtenerUsuariosPenalizados() {
        return userRepository.findAll()
                .stream()
                .filter(User::isBloqueado)
                .toList();
    }
}