package es.deusto.spq.deustocrai.service;

import es.deusto.spq.deustocrai.dao.UserRepository;
import es.deusto.spq.deustocrai.entity.User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PenalizacionService {

    private final UserRepository userRepository;
    private final NotificacionService notificacionService;

    public PenalizacionService(
            UserRepository userRepository,
            NotificacionService notificacionService
    ) {
        this.userRepository = userRepository;
        this.notificacionService = notificacionService;
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