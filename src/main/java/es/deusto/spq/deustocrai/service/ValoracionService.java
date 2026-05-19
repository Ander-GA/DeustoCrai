package es.deusto.spq.deustocrai.service;

import es.deusto.spq.deustocrai.dao.AbstractRecursoRepository;
import es.deusto.spq.deustocrai.dao.PrestamoRepository;
import es.deusto.spq.deustocrai.dao.ValoracionRepository;
import es.deusto.spq.deustocrai.entity.AbstractRecurso;
import es.deusto.spq.deustocrai.entity.Prestamo;
import es.deusto.spq.deustocrai.entity.User;
import es.deusto.spq.deustocrai.entity.Valoracion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ValoracionService {

    @Autowired
    private ValoracionRepository valoracionRepository;

    @Autowired
    private PrestamoRepository prestamoRepository;

    @Autowired
    private AbstractRecursoRepository recursoRepository;

    @Transactional
    public Valoracion dejarResena(User usuario, Long recursoId, int puntuacion, String comentario) {
        if (puntuacion < 1 || puntuacion > 5) {
            throw new IllegalArgumentException("La puntuación debe ser entre 1 y 5 estrellas.");
        }

        AbstractRecurso recurso = recursoRepository.findById(recursoId)
                .orElseThrow(() -> new IllegalArgumentException("Recurso no encontrado."));

        //Solo puedes valorar si has completado un préstamo del material.
        boolean historialValido = !prestamoRepository.findByUsuarioIdAndEstado(usuario.getId(), Prestamo.EstadoPrestamo.DEVUELTO)
                .stream()
                .filter(p -> p.getRecurso().getId().equals(recursoId))
                .toList()
                .isEmpty();

        if (!historialValido) {
            throw new IllegalStateException("Debes haber alquilado y devuelto este recurso para poder valorarlo.");
        }

        if (valoracionRepository.existsByUsuarioIdAndRecursoId(usuario.getId(), recursoId)) {
            throw new IllegalStateException("Ya has valorado este recurso.");
        }

        Valoracion valoracion = new Valoracion(usuario, recurso, puntuacion, comentario);
        return valoracionRepository.save(valoracion);
    }

    public List<Valoracion> obtenerResenasDeRecurso(Long recursoId) {
        return valoracionRepository.findByRecursoIdOrderByFechaDesc(recursoId);
    }
}