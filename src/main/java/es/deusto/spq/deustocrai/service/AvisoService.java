package es.deusto.spq.deustocrai.service;

import es.deusto.spq.deustocrai.dao.AvisoRepository;
import es.deusto.spq.deustocrai.entity.Aviso;
import es.deusto.spq.deustocrai.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AvisoService {

    private final AvisoRepository avisoRepository;

    public AvisoService(AvisoRepository avisoRepository) {
        this.avisoRepository = avisoRepository;
    }

    public Aviso crearAviso(User usuario, Aviso.TipoAviso tipo, String titulo, String mensaje) {
        Aviso aviso = new Aviso(usuario, tipo, titulo, mensaje);
        return avisoRepository.save(aviso);
    }

    public List<Aviso> obtenerAvisosUsuario(User usuario) {
        return avisoRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuario.getId());
    }

    public List<Aviso> obtenerAvisosNoLeidos(User usuario) {
        return avisoRepository.findByUsuarioIdAndLeidoFalseOrderByFechaCreacionDesc(usuario.getId());
    }

    public Aviso marcarComoLeido(Long avisoId) {
        Aviso aviso = avisoRepository.findById(avisoId)
                .orElseThrow(() -> new IllegalArgumentException("Aviso no encontrado"));

        aviso.setLeido(true);
        return avisoRepository.save(aviso);
    }
}