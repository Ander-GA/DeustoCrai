package es.deusto.spq.deustocrai.service;

import es.deusto.spq.deustocrai.dao.AbstractRecursoRepository;
import es.deusto.spq.deustocrai.dao.ColaEsperaRepository;
import es.deusto.spq.deustocrai.dao.PrestamoRepository;
import es.deusto.spq.deustocrai.entity.AbstractRecurso;
import es.deusto.spq.deustocrai.entity.ColaEspera;
import es.deusto.spq.deustocrai.entity.ColaEspera.EstadoCola;
import es.deusto.spq.deustocrai.entity.Prestamo;
import es.deusto.spq.deustocrai.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ColaEsperaService {

    private final ColaEsperaRepository colaEsperaRepository;
    private final AbstractRecursoRepository recursoRepository;
    private final PrestamoRepository prestamoRepository;

    public ColaEsperaService(
            ColaEsperaRepository colaEsperaRepository,
            AbstractRecursoRepository recursoRepository,
            PrestamoRepository prestamoRepository
    ) {
        this.colaEsperaRepository = colaEsperaRepository;
        this.recursoRepository = recursoRepository;
        this.prestamoRepository = prestamoRepository;
    }

    public ColaEspera apuntarseACola(User usuario, Long recursoId) {
        AbstractRecurso recurso = recursoRepository.findById(recursoId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el recurso"));

        if (recurso.isDisponible()) {
            throw new IllegalStateException("El recurso está disponible, no es necesario apuntarse a la cola");
        }

        boolean yaEstaEnCola = colaEsperaRepository.existsByUsuarioIdAndRecursoIdAndEstado(
                usuario.getId(),
                recursoId,
                EstadoCola.ACTIVA
        );

        if (yaEstaEnCola) {
            throw new IllegalStateException("El usuario ya está apuntado a la cola de este recurso");
        }

        ColaEspera cola = new ColaEspera(usuario, recurso);
        return colaEsperaRepository.save(cola);
    }

    public List<ColaEspera> obtenerColaActiva(Long recursoId) {
        return colaEsperaRepository.findByRecursoIdAndEstadoOrderByFechaEntradaAsc(
                recursoId,
                EstadoCola.ACTIVA
        );
    }

    public Optional<Prestamo> asignarPrimerUsuarioSiExiste(AbstractRecurso recurso) {
        Optional<ColaEspera> primeraEntradaOpt =
                colaEsperaRepository.findFirstByRecursoIdAndEstadoOrderByFechaEntradaAsc(
                        recurso.getId(),
                        EstadoCola.ACTIVA
                );

        if (primeraEntradaOpt.isEmpty()) {
            recurso.setDisponible(true);
            recursoRepository.save(recurso);
            return Optional.empty();
        }

        ColaEspera primeraEntrada = primeraEntradaOpt.get();

        primeraEntrada.setEstado(EstadoCola.ASIGNADA);
        colaEsperaRepository.save(primeraEntrada);

        recurso.setDisponible(false);
        recursoRepository.save(recurso);

        Prestamo nuevoPrestamo = new Prestamo(
                primeraEntrada.getUsuario(),
                recurso
        );

        Prestamo prestamoGuardado = prestamoRepository.save(nuevoPrestamo);

        return Optional.of(prestamoGuardado);
    }

    public int obtenerPosicion(Long recursoId, Long usuarioId) {

        List<ColaEspera> cola = colaEsperaRepository
                .findByRecursoIdAndEstadoOrderByFechaEntradaAsc(
                        recursoId,
                        EstadoCola.ACTIVA
                );

        for (int i = 0; i < cola.size(); i++) {
            if (cola.get(i).getUsuario().getId().equals(usuarioId)) {
                return i + 1;
            }
        }

        return -1;
    }
}